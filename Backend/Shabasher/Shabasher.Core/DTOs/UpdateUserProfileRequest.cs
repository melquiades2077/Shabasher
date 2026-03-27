namespace Shabasher.Core.DTOs
{
    public record UpdateUserProfileRequest(
        string Name,
        string? AboutMe = null,
        string? Telegram = null
        );
}
