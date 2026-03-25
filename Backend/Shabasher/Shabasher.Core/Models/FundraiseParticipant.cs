namespace Shabasher.Core.Models
{
    public class FundraiseParticipant
    {
        public string Id { get; }
        public string FundraiseId { get; }
        public string UserId { get; }
        public FundraiseParticipantStatus Status { get; }
        public DateTime PaidAt { get; }
        public DateTime? CheckedAt { get; } = null;

        public FundraiseParticipant(string fundraiseId, string userId)
        {
            Id = Guid.NewGuid().ToString();
            FundraiseId = fundraiseId;
            UserId = userId;
            Status = FundraiseParticipantStatus.Pending;
            PaidAt = DateTime.UtcNow;
        }
    }
}
