using Shabasher.Core.Models;

namespace Shabasher.DataManage.Entities
{
    public class UserEntity
    {
        public string Id { get; set; }

        public string Name { get; set; }

        public string Email { get; set; }

        public DateTime CreatedAt { get; set; }

        public string PasswordHash { get; private set; }

        public List<ShabashEntity> Shabashes { get; set; }
    }
}
