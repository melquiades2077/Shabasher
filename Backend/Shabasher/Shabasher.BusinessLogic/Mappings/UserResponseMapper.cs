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
                string.IsNullOrEmpty(user.AboutMe) ? null : user.AboutMe,
                string.IsNullOrEmpty(user.Telegram) ? null : user.Telegram,
                user.CreatedAt,
                new List<UserShabashParticipationResponse>());

        public static UserShortResponse DomainToShortResponse(User user) =>
            new UserShortResponse(user.Id, user.Name);

        public static UserResponse EntityToResponse(UserEntity userEntity) =>
            new UserResponse(
                userEntity.Id,
                userEntity.Name,
                userEntity.Email,
                string.IsNullOrEmpty(userEntity.AboutMe) ? null : userEntity.AboutMe,
                string.IsNullOrEmpty(userEntity.Telegram) ? null : userEntity.Telegram,
                userEntity.CreatedAt,
                userEntity.Participations?
                    .Select(p => new UserShabashParticipationResponse(
                        p.ShabashId,
                        p.Shabash?.Name ?? string.Empty,
                        p.Status,
                        p.Role))
                    .ToList() ?? new List<UserShabashParticipationResponse>());

        public static UserShortResponse EntityToShortResponse(UserEntity userEntity) =>
            new UserShortResponse(userEntity.Id, userEntity.Name);
    }
}
