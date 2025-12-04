namespace Shabasher.Core.Models
{
    public class Invite
    {
        private const int EXPIRES_DAYS = 3;

        public string Id { get; }

        public string ShabashId { get; }

        public string InviterUserId { get; }

        public DateTime CreatedAt { get; }

        public DateTime ExpiresAt => CreatedAt.AddDays(EXPIRES_DAYS);

        public Invite(string shabashId, string inviterUserId)
        {
            Id = Guid.NewGuid().ToString();
            ShabashId = shabashId;
            InviterUserId = inviterUserId;
            CreatedAt = DateTime.UtcNow;
        }

        private Invite(string id, string shabashId, string inviterUserId, DateTime createdAt)
        {
            Id = id;
            ShabashId = shabashId;
            InviterUserId = inviterUserId;
            CreatedAt = createdAt;
        }

        public static Invite FromEntity(string id,
                string shabashId,
                string inviterUserId,
                DateTime createdAt) => new Invite(id, shabashId, inviterUserId, createdAt);
    }
}
