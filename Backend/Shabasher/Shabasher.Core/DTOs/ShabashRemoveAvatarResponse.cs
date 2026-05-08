namespace Shabasher.Core.DTOs
{
    public record ShabashRemoveAvatarResponse(
        ShabashResponse Shabash,
        string OldObjectKey
    );
}


