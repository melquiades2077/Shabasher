using System.Linq;
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
                PasswordHash = user.PasswordHash
            };
        }

        public static User ToDomain(UserEntity userEntity)
        {
            return User.FromEntity(
                userEntity.Id,
                userEntity.Name,
                userEntity.Email,
                userEntity.PasswordHash,
                userEntity.CreatedAt);
        }
    }
}
