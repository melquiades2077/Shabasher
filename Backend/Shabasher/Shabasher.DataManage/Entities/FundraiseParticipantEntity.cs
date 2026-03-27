using Shabasher.Core.Models;

namespace Shabasher.DataManage.Entities
{
    public class FundraiseParticipantEntity
    {
        public string Id { get; set; }
        public string FundraiseId { get; set; }
        public string UserId { get; set; }
        public decimal Amount { get; set; }
        public FundraiseParticipantStatus Status { get; set; }
        public DateTime PaidAt { get; set; }
        public DateTime? CheckedAt { get; set; }
    }
}
