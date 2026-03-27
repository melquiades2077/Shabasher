namespace Shabasher.Core.Models
{
    public class SuggestionVote
    {
        public string Id { get; }
        public string SuggestionId { get; }
        public string UserId { get; }
        public Vote Vote { get; }
        public DateTime CreatedAt { get; }

        public SuggestionVote(string suggestionId, string userId, Vote vote)
        {
            Id = Guid.NewGuid().ToString();
            SuggestionId = suggestionId;
            UserId = userId;
            Vote = vote;
            CreatedAt = DateTime.UtcNow;
        }
    }
}
