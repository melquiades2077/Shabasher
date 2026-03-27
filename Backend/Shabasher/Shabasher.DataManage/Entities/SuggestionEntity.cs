namespace Shabasher.DataManage.Entities
{
    public class SuggestionEntity
    {
        public string Id { get; set; }
        public string ShabashId { get; set; }
        public string UserId { get; set; }
        public string Description { get; set; } = "";
        public int LikesCount { get; set; }
        public int DislikesCount { get; set; }
        public DateTime CreatedAt { get; set; }

        public ShabashEntity Shabash { get; set; }
        public UserEntity User { get; set; }

        public List<SuggestionVoteEntity> Votes { get; set; } = [];
    }
}
