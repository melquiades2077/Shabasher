namespace Shabasher.Core.DTOs
{
    public record UserResponse(
        string Id,
        string Name,
        string Email,
        string? AboutMe,
        string? Telegram,
        DateTime CreatedAt,
        List<UserShabashParticipationResponse> Participations
        );
}
