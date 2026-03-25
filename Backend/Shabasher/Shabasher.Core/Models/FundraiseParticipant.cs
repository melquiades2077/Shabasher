using CSharpFunctionalExtensions;

namespace Shabasher.Core.Models
{
    public class FundraiseParticipant
    {
        private const decimal MIN_DONATION_AMOUNT = 10;
        public string Id { get; }
        public string FundraiseId { get; }
        public string UserId { get; }
        public decimal Amount { get; }
        public FundraiseParticipantStatus Status { get; }
        public DateTime PaidAt { get; }
        public DateTime? CheckedAt { get; } = null;

        private FundraiseParticipant(string fundraiseId, string userId, decimal amount)
        {
            Id = Guid.NewGuid().ToString();
            FundraiseId = fundraiseId;
            UserId = userId;
            Status = FundraiseParticipantStatus.Pending;
            PaidAt = DateTime.UtcNow;
            Amount = amount;
        }

        public static Result<FundraiseParticipant> Create(string fundraiseId, string userId, decimal amount)
        {
            if (string.IsNullOrWhiteSpace(fundraiseId) || string.IsNullOrWhiteSpace(userId))
                return Result.Failure<FundraiseParticipant>("null fundraiseId/userId");
            if (amount < 10)
                return Result.Failure<FundraiseParticipant>($"Сумма оплаты должна быть не менее {MIN_DONATION_AMOUNT}");

            return Result.Success<FundraiseParticipant>(new FundraiseParticipant(fundraiseId, userId, amount));
        }
    }
}
