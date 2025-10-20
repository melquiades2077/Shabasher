using Shabasher.DataManage.Entities;
using Shabasher.Core.Models;

namespace Shabasher.DataManage.Mappings
{
    public static class UserEntityMapper
    {
        public static UserEntity ToEntity(User user)
        {
            return new UserEntity
            {
                Id = user.Id,
                Name = user.Name,
                Email = user.Email,
                CreatedAt = user.CreatedAt,
                PasswordHash = user.PasswordHash,
            };
        }
    }
}
