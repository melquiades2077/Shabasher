namespace Shabasher.DataManage.Entities
{
    public class UserEntity
    {
        public string Id { get; set; }

        public string Name { get; set; }

        public string Email { get; set; }

        public string AboutMe { get; set; }

        public string Telegram { get; set; }

        public DateTime CreatedAt { get; set; }

        public string PasswordHash { get; set; }

        public List<ShabashParticipantEntity> Participations { get; set; } = [];
    }
}
