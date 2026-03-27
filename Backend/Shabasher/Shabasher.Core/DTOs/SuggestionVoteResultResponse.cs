namespace Shabasher.Core.DTOs
{
    public record SuggestionVoteResultResponse(
        int Likes,
        int Dislikes,
        bool Liked,
        bool Disliked
    );
}
