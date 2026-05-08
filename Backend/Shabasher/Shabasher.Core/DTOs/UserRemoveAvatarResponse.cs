namespace Shabasher.Core.DTOs
{
    public record UserRemoveAvatarResponse(
        UserResponse User,
        string OldObjectKey
    );
}


