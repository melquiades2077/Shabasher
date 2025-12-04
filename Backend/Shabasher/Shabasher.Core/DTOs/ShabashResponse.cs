using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record ShabashResponse(
        string Id,
        string Name,
        string Description,
        List<ShabashParticipantResponse> Participants,
        DateOnly StartDate,
        TimeOnly StartTime,
        DateTime CreatedAt,
        ShabashStatus Status
        );
}