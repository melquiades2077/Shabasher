using CSharpFunctionalExtensions;
using Microsoft.EntityFrameworkCore;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage;
using Shabasher.DataManage.Entities;

namespace Shabasher.BusinessLogic.Services
{
    public class FundraisesManageService : IFundraisesManageService
    {
        private readonly ShabasherDbContext _dbcontext;

        public FundraisesManageService(ShabasherDbContext dbcontext)
        {
            _dbcontext = dbcontext;
        }

        private async Task<ShabashRole?> GetUserRoleInShabash(string shabashId, string userId)
        {
            return await _dbcontext.ShabashParticipants
                .AsNoTracking()
                .Where(p => p.ShabashId == shabashId && p.UserId == userId)
                .Select(p => (ShabashRole?)p.Role)
                .FirstOrDefaultAsync();
        }

        private async Task<bool> IsParticipantInShabash(string shabashId, string userId)
        {
            return await _dbcontext.ShabashParticipants
                .AsNoTracking()
                .AnyAsync(p => p.ShabashId == shabashId && p.UserId == userId);
        }

        private async Task<bool> HasConfirmedPayment(string fundraiseId, string userId)
        {
            return await _dbcontext.FundraiseParticipants
                .AsNoTracking()
                .AnyAsync(p => p.FundraiseId == fundraiseId && p.UserId == userId && p.Status == FundraiseParticipantStatus.Confirmed);
        }

        private static FundraisingItemResponse ToItem(FundraiseEntity f, FundraiseParticipantStatus? myStatus) =>
            new(
                f.Id,
                f.Name,
                f.EventId,
                f.CreatorId,
                f.CreatorPhone,
                f.CreatorName,
                f.Description,
                f.TargetAmount,
                f.CurrentAmount,
                f.FundStatus,
                f.CreatedAt,
                myStatus);

        public async Task<Result<FundraisesListResponse>> GetAllFundraisesAsync(string shabashId, string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure<FundraisesListResponse>("Пользователь не определён");

            if (!await IsParticipantInShabash(shabashId, userId))
                return Result.Failure<FundraisesListResponse>("Нет доступа к событию");

            var fundraises = await _dbcontext.Fundraises
                .AsNoTracking()
                .Where(f => f.EventId == shabashId)
                .OrderByDescending(f => f.CreatedAt)
                .ToListAsync();

            if (fundraises.Count == 0)
                return Result.Success(new FundraisesListResponse(Array.Empty<FundraisingItemResponse>()));

            var ids = fundraises.Select(f => f.Id).ToList();
            var myStatuses = await _dbcontext.FundraiseParticipants
                .AsNoTracking()
                .Where(p => p.UserId == userId && ids.Contains(p.FundraiseId))
                .ToDictionaryAsync(p => p.FundraiseId, p => p.Status);

            var list = fundraises
                .Select(f => ToItem(f, myStatuses.TryGetValue(f.Id, out var s) ? s : null))
                .ToList();

            return Result.Success(new FundraisesListResponse(list));
        }

        public async Task<Result<FundraiseDetailsResponse>> GetFundraiseAsync(string fundraiseId, string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure<FundraiseDetailsResponse>("Пользователь не определён");

            var fundraise = await _dbcontext.Fundraises
                .AsNoTracking()
                .FirstOrDefaultAsync(f => f.Id == fundraiseId);

            if (fundraise == null)
                return Result.Failure<FundraiseDetailsResponse>("Сбор не найден");

            var inEvent = await IsParticipantInShabash(fundraise.EventId, userId);
            var confirmed = await HasConfirmedPayment(fundraiseId, userId);
            if (!inEvent && !confirmed)
                return Result.Failure<FundraiseDetailsResponse>("Нет доступа к событию");

            var role = await GetUserRoleInShabash(fundraise.EventId, userId);
            var isAdmin = role == ShabashRole.Admin || role == ShabashRole.CoAdmin;

            var myStatus = await _dbcontext.FundraiseParticipants
                .AsNoTracking()
                .Where(p => p.FundraiseId == fundraiseId && p.UserId == userId)
                .Select(p => (FundraiseParticipantStatus?)p.Status)
                .FirstOrDefaultAsync();

            var confirmedCount = await _dbcontext.FundraiseParticipants
                .AsNoTracking()
                .CountAsync(p => p.FundraiseId == fundraiseId && p.Status == FundraiseParticipantStatus.Confirmed);

            var participantsCount = await _dbcontext.FundraiseParticipants
                .AsNoTracking()
                .CountAsync(p => p.FundraiseId == fundraiseId);

            List<FundraiseParticipantInfoResponse>? participants = null;
            if (isAdmin)
            {
                participants = await _dbcontext.FundraiseParticipants
                    .AsNoTracking()
                    .Where(p => p.FundraiseId == fundraiseId)
                    .OrderByDescending(p => p.PaidAt)
                    .Select(p => new FundraiseParticipantInfoResponse(
                        p.UserId,
                        p.Status,
                        p.Amount,
                        p.PaidAt,
                        p.CheckedAt))
                    .ToListAsync();
            }

            return Result.Success(new FundraiseDetailsResponse(
                ToItem(fundraise, myStatus),
                confirmedCount,
                participantsCount,
                participants));
        }

        public async Task<Result<FundraiseDetailsResponse>> CreateFundraiseAsync(string shabashId, string userId, CreateFundraiseRequest request)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure<FundraiseDetailsResponse>("Пользователь не определён");

            var role = await GetUserRoleInShabash(shabashId, userId);
            if (role == null)
                return Result.Failure<FundraiseDetailsResponse>("Нет доступа к событию");

            if (role != ShabashRole.Admin && role != ShabashRole.CoAdmin)
                return Result.Failure<FundraiseDetailsResponse>("Forbidden");

            var created = Fundraise.Create(
                request.Title,
                shabashId,
                userId,
                request.PaymentPhone,
                request.PaymentRecipient,
                request.Description ?? string.Empty,
                request.TargetAmount ?? 0);

            if (created.IsFailure)
                return Result.Failure<FundraiseDetailsResponse>(created.Error);

            var f = created.Value;
            var entity = new FundraiseEntity
            {
                Id = f.Id,
                Name = f.Name,
                EventId = f.EventId,
                CreatorId = f.CreatorId,
                CreatorPhone = f.CreatorPhone,
                CreatorName = f.CreatorName,
                Description = f.Description ?? string.Empty,
                TargetAmount = request.TargetAmount,
                CurrentAmount = 0,
                FundStatus = FundStatus.Active,
                CreatedAt = f.CreatedAt,
                Participants = []
            };

            _dbcontext.Fundraises.Add(entity);
            await _dbcontext.SaveChangesAsync();

            return Result.Success(new FundraiseDetailsResponse(
                ToItem(entity, null),
                ConfirmedCount: 0,
                ParticipantsCount: 0,
                Participants: new List<FundraiseParticipantInfoResponse>()));
        }

        public async Task<Result> CloseFundraiseAsync(string fundraiseId, string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure("Пользователь не определён");

            var fundraise = await _dbcontext.Fundraises.FirstOrDefaultAsync(f => f.Id == fundraiseId);
            if (fundraise == null)
                return Result.Failure("Сбор не найден");

            var role = await GetUserRoleInShabash(fundraise.EventId, userId);
            if (role != ShabashRole.Admin && role != ShabashRole.CoAdmin)
                return Result.Failure("Forbidden");

            fundraise.FundStatus = FundStatus.Closed;
            await _dbcontext.SaveChangesAsync();

            return Result.Success();
        }

        public async Task<Result> MarkPaidAsync(string fundraiseId, string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return Result.Failure("Пользователь не определён");

            var fundraise = await _dbcontext.Fundraises
                .AsNoTracking()
                .FirstOrDefaultAsync(f => f.Id == fundraiseId);
            if (fundraise == null)
                return Result.Failure("Сбор не найден");

            if (fundraise.FundStatus == FundStatus.Closed)
                return Result.Failure("Сбор закрыт");

            if (!await IsParticipantInShabash(fundraise.EventId, userId))
                return Result.Failure("Нет доступа к событию");

            var already = await _dbcontext.FundraiseParticipants
                .AnyAsync(p => p.FundraiseId == fundraiseId && p.UserId == userId);
            if (already)
                return Result.Failure("Conflict");

            _dbcontext.FundraiseParticipants.Add(new FundraiseParticipantEntity
            {
                Id = Guid.NewGuid().ToString(),
                FundraiseId = fundraiseId,
                UserId = userId,
                Amount = 0,
                Status = FundraiseParticipantStatus.Pending,
                PaidAt = DateTime.UtcNow,
                CheckedAt = null
            });

            await _dbcontext.SaveChangesAsync();
            return Result.Success();
        }

        public async Task<Result> ConfirmPaymentAsync(string fundraiseId, string targetUserId, string adminId, decimal? amount)
        {
            if (string.IsNullOrEmpty(adminId))
                return Result.Failure("Пользователь не определён");

            await using var tx = await _dbcontext.Database.BeginTransactionAsync();

            var fundraise = await _dbcontext.Fundraises.FirstOrDefaultAsync(f => f.Id == fundraiseId);
            if (fundraise == null)
                return Result.Failure("Сбор не найден");

            var role = await GetUserRoleInShabash(fundraise.EventId, adminId);
            if (role != ShabashRole.Admin && role != ShabashRole.CoAdmin)
                return Result.Failure("Недостаточно прав");

            var participant = await _dbcontext.FundraiseParticipants
                .FirstOrDefaultAsync(p => p.FundraiseId == fundraiseId && p.UserId == targetUserId);
            if (participant == null)
                return Result.Failure("Участник не найден");

            if (amount.HasValue && amount.Value < 0)
                return Result.Failure("Неверная сумма");

            var newAmount = amount ?? participant.Amount;
            if (participant.Status == FundraiseParticipantStatus.Confirmed)
            {
                var delta = newAmount - participant.Amount;
                participant.Amount = newAmount;
                fundraise.CurrentAmount += delta;
            }
            else
            {
                participant.Status = FundraiseParticipantStatus.Confirmed;
                participant.CheckedAt = DateTime.UtcNow;
                participant.Amount = newAmount;
                fundraise.CurrentAmount += newAmount;
            }

            await _dbcontext.SaveChangesAsync();
            await tx.CommitAsync();
            return Result.Success();
        }

        public async Task<Result> RevertPaymentAsync(string fundraiseId, string targetUserId, string adminId)
        {
            if (string.IsNullOrEmpty(adminId))
                return Result.Failure("Пользователь не определён");

            await using var tx = await _dbcontext.Database.BeginTransactionAsync();

            var fundraise = await _dbcontext.Fundraises.FirstOrDefaultAsync(f => f.Id == fundraiseId);
            if (fundraise == null)
                return Result.Failure("Сбор не найден");

            var role = await GetUserRoleInShabash(fundraise.EventId, adminId);
            if (role != ShabashRole.Admin && role != ShabashRole.CoAdmin)
                return Result.Failure("Forbidden");

            var participant = await _dbcontext.FundraiseParticipants
                .FirstOrDefaultAsync(p => p.FundraiseId == fundraiseId && p.UserId == targetUserId);
            if (participant == null)
                return Result.Failure("Участник не найден");

            if (participant.Status == FundraiseParticipantStatus.Confirmed)
            {
                fundraise.CurrentAmount -= participant.Amount;
            }

            participant.Status = FundraiseParticipantStatus.NotPaid;
            participant.CheckedAt = DateTime.UtcNow;
            participant.Amount = 0;

            await _dbcontext.SaveChangesAsync();
            await tx.CommitAsync();
            return Result.Success();
        }
    }
}
