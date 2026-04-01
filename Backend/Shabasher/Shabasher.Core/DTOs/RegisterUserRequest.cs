namespace Shabasher.Core.DTOs
{
    public record RegisterUserRequest(
        string Name,
        string Email,
        string Password,
        string? AboutMe = null,
        string? Telegram = null
        );
}
