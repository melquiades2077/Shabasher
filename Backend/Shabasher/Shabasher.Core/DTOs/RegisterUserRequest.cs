namespace Shabasher.Core.DTOs
{
    public record RegisterUserRequest(
        string Name,
        string Email,
        string Password
        );
}
