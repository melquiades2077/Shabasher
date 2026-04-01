namespace Shabasher.Core.DTOs
{
    public record KickParticipantRequest(
        string UserId, 
        string AdminId,
        string ShabashId
        );
}
