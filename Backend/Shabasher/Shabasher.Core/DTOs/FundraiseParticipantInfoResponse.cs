using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record FundraiseParticipantInfoResponse(
        string UserId,
        FundraiseParticipantStatus Status,
        decimal Amount,
        DateTime PaidAt,
        DateTime? CheckedAt
    );
}

