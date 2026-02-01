namespace Shabasher.Core.DTOs
{
    public record KickParticipantRequest(
        string UserId, 
        string ShabashId
        );
}
