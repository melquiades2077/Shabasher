namespace Shabasher.Core.DTOs
{
    public record UserResponse(
        string id,
        string name,
        string email,
        DateTime createdAt
        );
}
