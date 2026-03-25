namespace Shabasher.Core.DTOs
{
    public record CreateFundraiseRequest(
        string Title,
        string? Description,
        decimal? TargetAmount,
        string PaymentPhone,
        string PaymentRecipient
    );
}

