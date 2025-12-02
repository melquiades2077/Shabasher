using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record UpdateShabashRequest(
        string Id,
        string Name,
        string Description,
        List<ShabashParticipantResponse> Participants
        );
}