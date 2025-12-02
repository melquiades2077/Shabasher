namespace Shabasher.Core.DTOs
{
    public record UserResponse(
        string Id,
        string Name,
        string Email,
        DateTime CreatedAt,
        List<UserShabashParticipationResponse> Participations
        );
}
