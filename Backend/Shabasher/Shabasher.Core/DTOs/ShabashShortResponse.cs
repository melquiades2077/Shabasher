using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record ShabashShortResponse(
        string Id,
        string Name,
        DateOnly StartDate,
        TimeOnly StartTime,
        ShabashStatus Status
        );
}