namespace Shabasher.Core.Models
{
    public class ShabashParticipant
    {
        public User User { get; }

        public UserStatus Status { get; }

        public ShabashParticipant(User user, UserStatus status)
        {
            User = user;
            Status = status;
        }
    }
}


