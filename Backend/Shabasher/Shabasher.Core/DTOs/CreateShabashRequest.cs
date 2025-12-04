using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record CreateShabashRequest(
        string Name,
        string Description,
        DateOnly StartDate,
        TimeOnly StartTime,
        List<ShabashParticipantResponse>? Participants
        );
}

