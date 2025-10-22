namespace Shabasher.Core.DTOs
{
    public record CreateUserRequest(
        string Name,
        string Email,
        string Password
        );
}
