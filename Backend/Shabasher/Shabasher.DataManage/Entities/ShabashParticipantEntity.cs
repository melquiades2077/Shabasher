using Shabasher.Core.Models;

namespace Shabasher.DataManage.Entities
{
    public class ShabashParticipantEntity
    {
        public string ShabashId { get; set; }

        public ShabashEntity Shabash { get; set; }

        public string UserId { get; set; }

        public UserEntity User { get; set; }

        public UserStatus Status { get; set; }

        public ShabashRole Role { get; set; }
    }
}


