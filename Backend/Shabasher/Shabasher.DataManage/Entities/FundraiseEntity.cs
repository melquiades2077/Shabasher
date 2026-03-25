using Shabasher.Core.Models;

namespace Shabasher.DataManage.Entities
{
    public class FundraiseEntity
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public string EventId { get; set; }
        public string CreatorId { get; set; }
        public string CreatorPhone { get; set; }
        public string CreatorName { get; set; }
        public string Description { get; set; } = "";
        public decimal? TargetAmount { get; set; } = null;
        public decimal CurrentAmount { get; set; }
        public FundStatus FundStatus { get; set; }
        public DateTime CreatedAt { get; set; }
        public List<FundraiseParticipantEntity> Participants { get; set; } = [];
    }
}
