using CSharpFunctionalExtensions;

namespace Shabasher.Core.Models
{
    public class Suggestion
    {
        private const int MAX_DESCRIPTION_LEN = 300;

        public string Id { get; }
        public string ShabashId { get; }
        public string UserId { get; }
        public string Description { get; } = "";
        public int LikesCount => Votes.Count(v => v.Vote == Vote.Like);
        public int DislikesCount => Votes.Count(v => v.Vote == Vote.Dislike);
        public DateTime CreatedAt { get; }
        public List<SuggestionVote> Votes { get; }

        private Suggestion(string id, string shabashId, string userId, string description)
        {
            Id = id;
            ShabashId = shabashId;
            UserId = userId;
            Description = description;
            CreatedAt = DateTime.UtcNow;
            Votes = new List<SuggestionVote>();
        }

        public static Result<Suggestion> Create(string shabashId, string userId, string description)
        {
            description = (description ?? string.Empty).Trim();
            if (string.IsNullOrEmpty(description))
                return Result.Failure<Suggestion>("Текст предложения не может быть пустым");
            if (description.Length > MAX_DESCRIPTION_LEN)
                return Result.Failure<Suggestion>($"Длина описания предложения не должна превышать {MAX_DESCRIPTION_LEN}");

            string id = Guid.NewGuid().ToString();

            return Result.Success<Suggestion>(new Suggestion(id, shabashId, userId, description));
        }
    }
}
