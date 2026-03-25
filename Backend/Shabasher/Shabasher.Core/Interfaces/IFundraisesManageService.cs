using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;

namespace Shabasher.Core.Interfaces
{
    public interface IFundraisesManageService
    {
        Task<Result<FundraisesListResponse>> GetAllFundraisesAsync(string shabashId, string userId);

        Task<Result<FundraiseDetailsResponse>> GetFundraiseAsync(string fundraiseId, string userId);

        Task<Result<FundraiseDetailsResponse>> CreateFundraiseAsync(string shabashId, string userId, CreateFundraiseRequest request);

        Task<Result> CloseFundraiseAsync(string fundraiseId, string userId);

        Task<Result> MarkPaidAsync(string fundraiseId, string userId);

        Task<Result> ConfirmPaymentAsync(string fundraiseId, string targetUserId, string adminId, decimal? amount);

        Task<Result> RevertPaymentAsync(string fundraiseId, string targetUserId, string adminId);
    }
}
