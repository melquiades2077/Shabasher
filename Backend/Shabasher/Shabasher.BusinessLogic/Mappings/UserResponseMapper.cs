using Shabasher.Core.DTOs;
using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;

namespace Shabasher.BusinessLogic.Mappings
{
    public static class UserResponseMapper
    {
        public static UserResponse DomainToResponse(User user) => 
            new UserResponse(user.Id, user.Name, user.Email, user.CreatedAt);

        public static UserShortResponse DomainToShortResponse(User user) => 
            new UserShortResponse(user.Id, user.Name);
    
        public static UserResponse EntityToResponse(UserEntity userEntity) => 
            new UserResponse(userEntity.Id, userEntity.Name, userEntity.Email, userEntity.CreatedAt);

        public static UserShortResponse EntityToShortResponse(UserEntity userEntity) =>
            new UserShortResponse(userEntity.Id, userEntity.Name);
    }
}
