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
                shabash.Participants
                    .Select(p => new ShabashParticipantResponse(
                        UserResponseMapper.DomainToResponse(p.User),
                        p.Status))
                    .ToList(),
                shabash.CreatedAt);

        public static ShabashShortResponse DomainToShortResponse(Shabash shabash) =>
            new ShabashShortResponse(shabash.Id, shabash.Name);

        public static ShabashResponse EntityToResponse(ShabashEntity shabashEntity) =>
            new ShabashResponse(
                shabashEntity.Id,
                shabashEntity.Name,
                shabashEntity.Description,
                shabashEntity.Participants
                    .Select(p => new ShabashParticipantResponse(
                        UserResponseMapper.EntityToResponse(p.User),
                        p.Status))
                    .ToList(),
                shabashEntity.CreatedAt);

        public static ShabashShortResponse EntityToShortResponse(ShabashEntity shabashEntity) =>
            new ShabashShortResponse(shabashEntity.Id, shabashEntity.Name);
    }
}
