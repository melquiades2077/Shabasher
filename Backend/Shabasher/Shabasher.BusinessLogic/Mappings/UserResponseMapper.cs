using System.Linq;
using Shabasher.Core.DTOs;
using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;

namespace Shabasher.BusinessLogic.Mappings
{
    public static class UserResponseMapper
    {
        public static UserResponse DomainToResponse(User user) =>
            new UserResponse(
                user.Id,
                user.Name,
                user.Email,
                user.CreatedAt,
                new List<UserShabashParticipationResponse>());

        public static UserShortResponse DomainToShortResponse(User user) =>
            new UserShortResponse(user.Id, user.Name);

        public static UserResponse EntityToResponse(UserEntity userEntity) =>
            new UserResponse(
                userEntity.Id,
                userEntity.Name,
                userEntity.Email,
                userEntity.CreatedAt,
                userEntity.Participations?
                    .Select(p => new UserShabashParticipationResponse(
                        p.ShabashId,
                        p.Shabash?.Name ?? string.Empty,
                        p.Status))
                    .ToList() ?? new List<UserShabashParticipationResponse>());

        public static UserShortResponse EntityToShortResponse(UserEntity userEntity) =>
            new UserShortResponse(userEntity.Id, userEntity.Name);
    }
}
