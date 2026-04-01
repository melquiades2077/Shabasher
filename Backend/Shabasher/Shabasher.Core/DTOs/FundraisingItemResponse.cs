using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record FundraisingItemResponse(
        string Id,
        string Title,
        string ShabashId,
        string CreatorId,
        string PaymentPhone,
        string PaymentRecipient,
        string? Description,
        decimal? TargetAmount,
        decimal CurrentAmount,
        FundStatus FundStatus,
        DateTime CreatedAt,
        FundraiseParticipantStatus? MyPaymentStatus
    );
}

