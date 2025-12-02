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

        public async Task<Result<string>> CreateShabashAsync(string name, string description, DateTime startDate, List<ShabashParticipant> participants)
        {
            var shabash = Shabash.Create(name, description, startDate, participants);

            if (shabash.IsFailure)
                return Result.Failure<string>(shabash.Error);

            _dbcontext.Shabashes.Add(ShabashEntityMapper.ToEntity(shabash.Value));
            await _dbcontext.SaveChangesAsync();

            return Result.Success<string>(shabash.Value.Id);
        }

        public async Task<Result<ShabashResponse>> UpdateShabashAsync(string shabashId, UpdateShabashRequest request, string userId)
        {
            var shabashEntity = GetShabashEntity(shabashId).Result;

            if (shabashEntity == null)
                return Result.Failure<ShabashResponse>("Шабаш не найден");

            shabashEntity.Name = request.Name;
            shabashEntity.Description = request.Description;
            shabashEntity.StartDate = request.StartDate.ToDateTime(request.StartTime);
            shabashEntity.Participants = request.Participants
                .Select(p => new ShabashParticipantEntity
                {
                    ShabashId = shabashEntity.Id,
                    UserId = p.User.Id,
                    Status = p.Status,
                    Role = p.Role
                })
                .ToList();

            await _dbcontext.SaveChangesAsync();

            return Result.Success(ShabashResponseMapper.EntityToResponse(shabashEntity));
        }

        public async Task<Result<string>> DeleteShabashAsync(string shabashId, string userId)
        {
            var shabashEntity = GetShabashEntity(shabashId).Result;

            if (shabashEntity == null)
                return Result.Failure<string>("Шабаш не найден");

            var user = shabashEntity.Participants.FirstOrDefault(p => p.UserId == userId);

            if (user == null)
                return Result.Failure<string>("Пользователь не найден");

            if (user.Role != ShabashRole.Admin)
                return Result.Failure<string>("У пользователя недостаточно прав");

            return Result.Success(shabashId);
        }

        public async Task<Result<ShabashResponse>> GetShabashByIdAsync(string shabashId)
        {
            var shabashEntity = GetShabashEntity(shabashId).Result;

            if (shabashEntity == null)
                return Result.Failure<ShabashResponse>("Шабаш не найден");

            return Result.Success(ShabashResponseMapper.EntityToResponse(shabashEntity));
        }
    }
}
