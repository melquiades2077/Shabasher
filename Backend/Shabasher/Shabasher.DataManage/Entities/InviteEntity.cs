namespace Shabasher.Core.Models
{
    public class InviteEntity
    {
        public string Id { get; set; }

        public string ShabashId { get; set; }

        public string InviterUserId { get; set; }

        public DateTime CreatedAt { get; set; }
    }
}