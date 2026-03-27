namespace Shabasher.DataManage.Entities
{
    public class RefreshTokenEntity
    {
        public int Id { get; set; }
        public string Token { get; set; } = string.Empty;
        public string JwtId { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public DateTime ExpiresAt { get; set; }
        public bool IsUsed { get; set; } = false;
        public bool IsRevoked { get; set; } = false;
        public string UserId { get; set; } = string.Empty;
    }
}
