using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record UserShabashParticipationResponse(
        string ShabashId,
        string ShabashName,
        UserStatus Status,
        ShabashRole Role
    );
}


