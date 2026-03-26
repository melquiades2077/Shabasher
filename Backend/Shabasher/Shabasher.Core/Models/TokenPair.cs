namespace Shabasher.Core.Models
{
    public class TokenPair
    {
        public string AccessToken { get; set; } = string.Empty;
        public string RefreshToken { get; set; } = string.Empty;
        public DateTime AccessTokenExpiration { get; set; }
    }
}
