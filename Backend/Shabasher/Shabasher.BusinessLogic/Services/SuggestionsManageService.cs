using CSharpFunctionalExtensions;
using Microsoft.EntityFrameworkCore;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage;
using Shabasher.DataManage.Entities;

namespace Shabasher.BusinessLogic.Services
{
    public class SuggestionsManageService : ISuggestionsManageService
    {
        private readonly ShabasherDbContext _db;

        public SuggestionsManageService(ShabasherDbContext db)
        {
            _db = db;
        }

        private static SuggestionResponse ToResponse(SuggestionEntity s, Vote? myVote) =>
            new(
                s.Id,
                s.UserId,
                s.User.Name,
                s.Description,
                s.LikesCount,
                s.DislikesCount,
                s.CreatedAt,
                myVote == Vote.Like,
                myVote == Vote.Dislike);

        private async Task<bool> IsParticipantAsync(string shabashId, string userId) =>
            await _db.ShabashParticipants.AnyAsync(p => p.ShabashId == shabashId && p.UserId == userId);

        public async Task<Result<SuggestionsListResponse>> GetSuggestionsAsync(string eventId, string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure<SuggestionsListResponse>("Пользователь не определён");

            if (!await IsParticipantAsync(eventId, userId))
                return Result.Failure<SuggestionsListResponse>("Нет доступа к событию");

            var suggestions = await _db.Suggestions
                .AsNoTracking()
                .Where(s => s.ShabashId == eventId)
                .OrderByDescending(s => s.CreatedAt)
                .ToListAsync();

            if (suggestions.Count == 0)
                return Result.Success(new SuggestionsListResponse(Array.Empty<SuggestionResponse>()));

            var ids = suggestions.Select(s => s.Id).ToList();
            var myVotes = await _db.SuggestionVotes
                .AsNoTracking()
                .Where(v => v.UserId == userId && ids.Contains(v.SuggestionId))
                .ToDictionaryAsync(v => v.SuggestionId, v => v.Vote);

            var list = suggestions
                .Select(s => ToResponse(s, myVotes.TryGetValue(s.Id, out var v) ? v : null))
                .ToList();

            return Result.Success(new SuggestionsListResponse(list));
        }

        public async Task<Result<SuggestionResponse>> CreateSuggestionAsync(string eventId, string userId, string text)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure<SuggestionResponse>("Пользователь не определён");

            if (!await IsParticipantAsync(eventId, userId))
                return Result.Failure<SuggestionResponse>("Нет доступа к событию");

            var created = Suggestion.Create(eventId, userId, text);
            if (created.IsFailure)
                return Result.Failure<SuggestionResponse>(created.Error);

            var s = created.Value;
            var entity = new SuggestionEntity
            {
                Id = s.Id,
                ShabashId = s.ShabashId,
                UserId = s.UserId,
                Description = s.Description,
                LikesCount = 0,
                DislikesCount = 0,
                CreatedAt = s.CreatedAt
            };

            _db.Suggestions.Add(entity);
            await _db.SaveChangesAsync();

            return Result.Success(ToResponse(entity, null));
        }

        public async Task<Result<SuggestionVoteResultResponse>> VoteAsync(string suggestionId, string userId, string action)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure<SuggestionVoteResultResponse>("Пользователь не определён");

            var normalized = action?.Trim().ToLowerInvariant();
            if (normalized != "like" && normalized != "dislike")
                return Result.Failure<SuggestionVoteResultResponse>("action должен быть \"like\" или \"dislike\"");

            await using var tx = await _db.Database.BeginTransactionAsync();

            var suggestion = await _db.Suggestions.FirstOrDefaultAsync(s => s.Id == suggestionId);
            if (suggestion == null)
                return Result.Failure<SuggestionVoteResultResponse>("Предложение не найдено");

            if (!await IsParticipantAsync(suggestion.ShabashId, userId))
                return Result.Failure<SuggestionVoteResultResponse>("Нет доступа к событию");

            var existing = await _db.SuggestionVotes
                .FirstOrDefaultAsync(v => v.SuggestionId == suggestionId && v.UserId == userId);

            Vote? current = existing?.Vote;
            var isLikeAction = normalized == "like";

            if (isLikeAction)
            {
                if (current == null)
                {
                    _db.SuggestionVotes.Add(new SuggestionVoteEntity
                    {
                        Id = Guid.NewGuid().ToString(),
                        SuggestionId = suggestionId,
                        UserId = userId,
                        Vote = Vote.Like,
                        CreatedAt = DateTime.UtcNow
                    });
                    suggestion.LikesCount++;
                }
                else if (current == Vote.Like)
                {
                    _db.SuggestionVotes.Remove(existing!);
                    suggestion.LikesCount--;
                }
                else
                {
                    existing!.Vote = Vote.Like;
                    suggestion.DislikesCount--;
                    suggestion.LikesCount++;
                }
            }
            else
            {
                if (current == null)
                {
                    _db.SuggestionVotes.Add(new SuggestionVoteEntity
                    {
                        Id = Guid.NewGuid().ToString(),
                        SuggestionId = suggestionId,
                        UserId = userId,
                        Vote = Vote.Dislike,
                        CreatedAt = DateTime.UtcNow
                    });
                    suggestion.DislikesCount++;
                }
                else if (current == Vote.Dislike)
                {
                    _db.SuggestionVotes.Remove(existing!);
                    suggestion.DislikesCount--;
                }
                else
                {
                    existing!.Vote = Vote.Dislike;
                    suggestion.LikesCount--;
                    suggestion.DislikesCount++;
                }
            }

            await _db.SaveChangesAsync();
            await tx.CommitAsync();

            var voteRow = await _db.SuggestionVotes
                .AsNoTracking()
                .FirstOrDefaultAsync(v => v.SuggestionId == suggestionId && v.UserId == userId);
            Vote? myVote = voteRow?.Vote;

            return Result.Success(new SuggestionVoteResultResponse(
                suggestion.LikesCount,
                suggestion.DislikesCount,
                myVote == Vote.Like,
                myVote == Vote.Dislike));
        }

        public async Task<Result> DeleteSuggestionAsync(string suggestionId, string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure("Пользователь не определён");

            var suggestion = await _db.Suggestions.FirstOrDefaultAsync(s => s.Id == suggestionId);
            if (suggestion == null)
                return Result.Failure("Предложение не найдено");

            if (suggestion.UserId != userId)
            {
                var role = await _db.ShabashParticipants
                    .AsNoTracking()
                    .Where(p => p.ShabashId == suggestion.ShabashId && p.UserId == userId)
                    .Select(p => p.Role)
                    .FirstOrDefaultAsync();

                if (role != ShabashRole.Admin && role != ShabashRole.CoAdmin)
                    return Result.Failure("Недостаточно прав для удаления");
            }

            _db.Suggestions.Remove(suggestion);
            await _db.SaveChangesAsync();

            return Result.Success();
        }
    }
}
