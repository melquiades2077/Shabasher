using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record ShabashResponse(
        string Id,
        string Name,
        string Description,
        string Address,
        string? AvatarUrl,
        List<ShabashParticipantResponse> Participants,
        DateOnly StartDate,
        TimeOnly StartTime,
        DateTime CreatedAt,
        ShabashStatus Status,
        ShabashRole ActorRole,
        UserStatus ActorStatus
        );
}