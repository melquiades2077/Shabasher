using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record UpdateRoleRequest(
        string ShabashId, 
        string UserId, 
        string AdminId, 
        ShabashRole Role
        );
}
