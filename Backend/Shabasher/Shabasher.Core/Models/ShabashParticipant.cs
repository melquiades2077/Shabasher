namespace Shabasher.Core.Models
{
    public class ShabashParticipant
    {
        public User User { get; }

        public UserStatus Status { get; }

        public ShabashRole Role { get; }

        public DateTime CreatedAt { get; }

        public ShabashParticipant(User user, UserStatus status, ShabashRole role)
        {
            User = user;
            Status = status;
            Role = role;
            CreatedAt = DateTime.UtcNow;
        }
    }
}


