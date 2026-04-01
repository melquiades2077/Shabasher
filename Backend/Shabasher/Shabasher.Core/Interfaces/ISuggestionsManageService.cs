using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;

namespace Shabasher.Core.Interfaces
{
    public interface ISuggestionsManageService
    {
        Task<Result<SuggestionsListResponse>> GetSuggestionsAsync(string eventId, string userId);

        Task<Result<SuggestionResponse>> CreateSuggestionAsync(string eventId, string userId, string text);

        Task<Result<SuggestionVoteResultResponse>> VoteAsync(string suggestionId, string userId, string action);

        Task<Result> DeleteSuggestionAsync(string suggestionId, string userId);
    }
}
