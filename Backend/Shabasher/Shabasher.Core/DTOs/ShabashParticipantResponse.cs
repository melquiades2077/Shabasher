using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record ShabashParticipantResponse(
        UserResponse User,
        UserStatus Status,
        ShabashRole Role
    );
}


