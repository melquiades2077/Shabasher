namespace Shabasher.Core.Models
{
    public class Invite
    {
        public string Id { get; }

        public string ShabashId { get; }

        public string InviterUserId { get; }

        public DateTime CreatedAt { get; }

        public Invite(string shabashId, string inviterUserId)
        {
            Id = Guid.NewGuid().ToString();
            ShabashId = shabashId;
            InviterUserId = inviterUserId;
            CreatedAt = DateTime.UtcNow;
        }
    }
}
