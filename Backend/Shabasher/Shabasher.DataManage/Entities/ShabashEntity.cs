namespace Shabasher.DataManage.Entities
{
    public class ShabashEntity
    {
        public string Id { get; set; }

        public string Name { get; set; }

        public string Description { get; set; } = "";

        public string Address { get; set; } = "";

        public List<ShabashParticipantEntity> Participants { get; set; } = [];

        public DateTime StartDate { get; set; }

        public DateTime CreatedAt { get; set; }
    }
}
