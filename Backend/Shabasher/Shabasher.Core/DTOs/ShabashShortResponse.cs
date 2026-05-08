using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record ShabashShortResponse(
        string Id,
        string Name,
        string? AvatarUrl,
        DateOnly StartDate,
        TimeOnly StartTime,
        ShabashStatus Status
        );
}