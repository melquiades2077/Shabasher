using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record UpdateShabashRequest(
        string Id,
        string Name,
        string Description,
        string Address,
        DateOnly StartDate,
        TimeOnly StartTime
        );
}