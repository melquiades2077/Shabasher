using CSharpFunctionalExtensions;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.Core.DTOs;
using Shabasher.DataManage;
using Shabasher.DataManage.Mappings;
using Shabasher.DataManage.Entities;
using Shabasher.BusinessLogic.Mappings;
using Microsoft.EntityFrameworkCore;

namespace Shabasher.BusinessLogic.Services
{
    public class ShabashesManageService : IShabashesManageService
    {
        private const string BASE_URL = "http://213.171.27.237:5000";

        private readonly ShabasherDbContext _dbcontext;

        public ShabashesManageService(ShabasherDbContext dbContext)
        {
            _dbcontext = dbContext;
        }

        private async Task<ShabashEntity> GetShabashEntity(string shabashId)
        {
            return await _dbcontext.Shabashes
                .Include(s => s.Participants)
                .ThenInclude(p => p.User)
                .FirstOrDefaultAsync(s => s.Id == shabashId);
        }

        //так делать не надо!!!!!
        public async Task<Result<string>> CreateShabashAsync(string name, string description, string address, DateTime startDate, string creatorUserId, List<ShabashParticipant> participants)
        {
            var creatorEntity = await _dbcontext.Users
                .AsNoTracking()
                .FirstOrDefaultAsync(u => u.Id == creatorUserId);

            if (creatorEntity == null)
                return Result.Failure<string>("Создатель шабаша не найден");

            var creatorInParticipants = participants.FirstOrDefault(p => p.User.Id == creatorUserId);

            if (creatorInParticipants == null)
            {
                var creatorUser = UserEntityMapper.ToDomain(creatorEntity);
                participants.Add(new ShabashParticipant(creatorUser, UserStatus.Invited, ShabashRole.Admin));
            }
            else
            {
                var index = participants.IndexOf(creatorInParticipants);
                participants[index] = new ShabashParticipant(creatorInParticipants.User, creatorInParticipants.Status, ShabashRole.Admin);
            }

            var shabash = Shabash.Create(name, description, address, startDate, participants);

            if (shabash.IsFailure)
                return Result.Failure<string>(shabash.Error);

            var shabashEntity = ShabashEntityMapper.ToEntity(shabash.Value);
            _dbcontext.Shabashes.Add(shabashEntity);
            await _dbcontext.SaveChangesAsync();

            foreach (var participant in shabash.Value.Participants)
            {
                var participantEntity = new ShabashParticipantEntity
                {
                    ShabashId = shabashEntity.Id,
                    UserId = participant.User.Id,
                    Status = participant.Status,
                    Role = participant.Role
                };
                _dbcontext.ShabashParticipants.Add(participantEntity);
            }

            await _dbcontext.SaveChangesAsync();

            return Result.Success<string>(shabash.Value.Id);
        }

        public async Task<Result<ShabashResponse>> UpdateShabashAsync(string shabashId, UpdateShabashRequest request, string userId)
        {
            var shabashEntity = await GetShabashEntity(shabashId);

            if (shabashEntity == null)
                return Result.Failure<ShabashResponse>("Шабаш не найден");

            shabashEntity.Name = request.Name;
            shabashEntity.Description = request.Description;
            shabashEntity.Address = request.Address;
            var localStart = request.StartDate.ToDateTime(request.StartTime);
            shabashEntity.StartDate = DateTime.SpecifyKind(localStart, DateTimeKind.Utc);

            await _dbcontext.SaveChangesAsync();

            return Result.Success(ShabashResponseMapper.EntityToResponse(shabashEntity));
        }

        public async Task<Result<string>> DeleteShabashAsync(string shabashId, string userId)
        {
            var shabashEntity = await GetShabashEntity(shabashId);

            if (shabashEntity == null)
                return Result.Failure<string>("Шабаш не найден");

            var user = shabashEntity.Participants.FirstOrDefault(p => p.UserId == userId);

            if (user == null)
                return Result.Failure<string>("Пользователь не найден");

            if (user.Role != ShabashRole.Admin)
                return Result.Failure<string>("У пользователя недостаточно прав");

            _dbcontext.Shabashes.Remove(shabashEntity);
            await _dbcontext.SaveChangesAsync();

            return Result.Success(shabashId);
        }

        public async Task<Result<ShabashResponse>> GetShabashByIdAsync(string shabashId)
        {
            var shabashEntity = await GetShabashEntity(shabashId);

            if (shabashEntity == null)
                return Result.Failure<ShabashResponse>("Шабаш не найден");

            return Result.Success(ShabashResponseMapper.EntityToResponse(shabashEntity));
        }

        public async Task<Result<string>> CreateInviteAsync(string shabashId, string userId)
        {
            var invite = new Invite(shabashId, userId);

            _dbcontext.Invites.Add(InviteEntityMapper.ToEntity(invite));
            await _dbcontext.SaveChangesAsync();

            var link = $"{BASE_URL}/api/invites/{invite.Id}";

            return Result.Success(link);
        }

        public async Task<Result<Invite>> GetInviteAsync(string inviteId)
        {
            var inviteEntity = await _dbcontext.Invites.AsNoTracking().FirstOrDefaultAsync(i => i.Id == inviteId);

            if (inviteEntity == null)
                return Result.Failure<Invite>("Приглашение не найдено");

            return Result.Success(Invite.FromEntity(inviteEntity.Id, inviteEntity.ShabashId, inviteEntity.InviterUserId, inviteEntity.CreatedAt));
        }

        public async Task<Result<UserShabashParticipationResponse>> JoinShabashAsync(string userId, string shabashId)
        {
            var userEntity = await _dbcontext.Users.FirstOrDefaultAsync(u => u.Id == userId);

            if (userEntity == null)
                return Result.Failure<UserShabashParticipationResponse>("Пользователь не найден");

            var shabashEntity = await GetShabashEntity(shabashId);

            if (shabashEntity == null)
                return Result.Failure<UserShabashParticipationResponse>("Шабаш не найден");

            var sp = new ShabashParticipantEntity
            {
                Shabash = shabashEntity,
                ShabashId = shabashId,
                User = userEntity,
                UserId = userId,
                Role = ShabashRole.Member,
                Status = UserStatus.Invited
            };

            var alreadyParticipating = await _dbcontext.ShabashParticipants.AnyAsync(p => p.UserId == userId && p.ShabashId == shabashId);
            if (alreadyParticipating)
                return Result.Failure<UserShabashParticipationResponse>("Вы уже участвуете в этом шабаше");

            await _dbcontext.ShabashParticipants.AddAsync(sp);
            await _dbcontext.SaveChangesAsync();

            return Result.Success(new UserShabashParticipationResponse(sp.ShabashId, sp.Shabash.Name, sp.Status));
        }
    }
}
