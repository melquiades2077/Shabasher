using Shabasher.Core.Models;

namespace Shabasher.DataManage.Entities
{
    public class SuggestionVoteEntity
    {
        public string Id { get; set; }
        public string SuggestionId { get; set; }
        public string UserId { get; set; }
        public Vote Vote { get; set; }
        public DateTime CreatedAt { get; set; }

        public SuggestionEntity Suggestion { get; set; }
        public UserEntity User { get; set; }
    }
}
