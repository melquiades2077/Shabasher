using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record UpdateUserStatusRequest(
        string ShabashId,
        UserStatus Status
        );
}