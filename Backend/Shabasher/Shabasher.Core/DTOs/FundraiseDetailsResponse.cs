namespace Shabasher.Core.DTOs
{
    public record FundraiseDetailsResponse(
        FundraisingItemResponse Fundraising,
        int ConfirmedCount,
        int ParticipantsCount,
        List<FundraiseParticipantInfoResponse>? Participants
    );
}

