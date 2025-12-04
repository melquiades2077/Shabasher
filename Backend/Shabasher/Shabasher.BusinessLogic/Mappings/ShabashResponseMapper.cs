using Shabasher.Core.DTOs;
using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;
using Shabasher.DataManage.Mappings;
using System.Linq;

namespace Shabasher.BusinessLogic.Mappings
{
    public static class ShabashResponseMapper
    {
        public static ShabashResponse DomainToResponse(Shabash shabash) =>
            new ShabashResponse(
                shabash.Id,
                shabash.Name,
                shabash.Description,
                shabash.Address,
                shabash.Participants
                    .Select(p => new ShabashParticipantResponse(
                        UserResponseMapper.DomainToResponse(p.User),
                        p.Status,
                        p.Role))
                    .ToList(),
                DateOnly.FromDateTime(shabash.StartDate),
                TimeOnly.FromDateTime(shabash.StartDate),
                shabash.CreatedAt,
                shabash.Status);

        public static ShabashShortResponse DomainToShortResponse(Shabash shabash) =>
            new ShabashShortResponse(
                shabash.Id,
                shabash.Name,
                DateOnly.FromDateTime(shabash.StartDate),
                TimeOnly.FromDateTime(shabash.StartDate),
                shabash.Status);

        public static ShabashResponse EntityToResponse(ShabashEntity shabashEntity) =>
            new ShabashResponse(
                shabashEntity.Id,
                shabashEntity.Name,
                shabashEntity.Description,
                shabashEntity.Address,
                shabashEntity.Participants
                    .Select(p => new ShabashParticipantResponse(
                        UserResponseMapper.EntityToResponse(p.User),
                        p.Status,
                        p.Role))
                    .ToList(),
                DateOnly.FromDateTime(shabashEntity.StartDate),
                TimeOnly.FromDateTime(shabashEntity.StartDate),
                shabashEntity.CreatedAt,
                DateTime.UtcNow >= shabashEntity.StartDate
                    ? ShabashStatus.Finished
                    : ShabashStatus.Active);

        public static ShabashShortResponse EntityToShortResponse(ShabashEntity shabashEntity) =>
            new ShabashShortResponse(
                shabashEntity.Id,
                shabashEntity.Name,
                DateOnly.FromDateTime(shabashEntity.StartDate),
                TimeOnly.FromDateTime(shabashEntity.StartDate),
                DateTime.UtcNow >= shabashEntity.StartDate
                    ? ShabashStatus.Finished
                    : ShabashStatus.Active);
    }
}
