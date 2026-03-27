namespace Shabasher.BusinessLogic.Jwt
{
    public class JwtOptions
    {
        public string SecretKey { get; set; } = string.Empty;

        public int AccessTokenExpiresMinutes { get; set; } = 15;
        public int RefreshTokenExpirationDays { get; set; } = 7;
    }
}
