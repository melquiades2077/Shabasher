using CSharpFunctionalExtensions;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage;
using Shabasher.DataManage.Entities;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

namespace Shabasher.BusinessLogic.Jwt
{
    public class JwtProvider : IJwtProvider
    {
        private readonly JwtOptions _jwtOptions;
        private readonly ShabasherDbContext _db;
        private readonly TokenValidationParameters _tvp;
        private readonly ILogger<JwtProvider> _logger;

        public JwtProvider(IOptions<JwtOptions> jwtOptions, ShabasherDbContext db, TokenValidationParameters tvp, ILogger<JwtProvider> logger)
        {
            _jwtOptions = jwtOptions.Value;
            _db = db;
            _tvp = tvp;
            _logger = logger;
        }

        public async Task<Result<TokenPair>> GenerateTokens(UserResponse user)
        {
            var jti = Guid.NewGuid().ToString();
            var expires = DateTime.UtcNow.AddMinutes(_jwtOptions.AccessTokenExpiresMinutes);

            Claim[] claims = [new("userId", user.Id), new(JwtRegisteredClaimNames.Jti, jti)];

            var signingCredentials = new SigningCredentials(
            new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_jwtOptions.SecretKey)),
            SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
            claims: claims,
            signingCredentials: signingCredentials,
            expires: expires);

            var accessToken = new JwtSecurityTokenHandler().WriteToken(token);
            var refreshToken = new RefreshTokenEntity
            {
                Token = GenerateRandomToken(),
                JwtId = jti,
                UserId = user.Id,
                CreatedAt = DateTime.UtcNow,
                ExpiresAt = DateTime.UtcNow.AddDays(_jwtOptions.RefreshTokenExpirationDays)
            };

            await _db.RefreshTokens.AddAsync(refreshToken);
            await _db.SaveChangesAsync();

            return Result.Success(new TokenPair
            {
                AccessToken = accessToken,
                RefreshToken = refreshToken.Token,
                AccessTokenExpiration = expires
            });
        }

        public async Task<Result<TokenPair>> RefreshTokens(string accessToken, string refreshToken)
        {
            var handler = new JwtSecurityTokenHandler();
            var paramsNoLifetime = _tvp.Clone();
            paramsNoLifetime.ValidateLifetime = false;

            ClaimsPrincipal principal;
            SecurityToken validatedToken;

            try
            {
                principal = handler.ValidateToken(accessToken, paramsNoLifetime, out validatedToken);
            }
            catch (Exception)
            {
                return Result.Failure<TokenPair>("Невалидный access token");
            }

            if (validatedToken is not JwtSecurityToken jwt ||
                !jwt.Header.Alg.Equals(SecurityAlgorithms.HmacSha256,
                    StringComparison.InvariantCultureIgnoreCase))
            {
                return Result.Failure<TokenPair>("Невалидный токен");
            }

            var stored = await _db.RefreshTokens
                .FirstOrDefaultAsync(r => r.Token == refreshToken);

            if (stored is null)
                return Result.Failure<TokenPair>("Refresh token не найден");

            var jti = principal.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;
            if (stored.JwtId != jti)
                return Result.Failure<TokenPair>("Токены не связаны");

            if (stored.IsRevoked)
                return Result.Failure<TokenPair>("Токен отозван");

            if (stored.IsUsed)
            {
                _logger.LogCritical(
                    "Отзываем все токены пользователя {UserId}",
                    stored.UserId);
                await RevokeAllUserTokens(stored.UserId);
                return Result.Failure<TokenPair>("Подозрительная активность. Все сессии завершены.");
            }

            if (stored.ExpiresAt < DateTime.UtcNow)
                return Result.Failure<TokenPair>("Refresh token истёк");

            stored.IsUsed = true;
            _db.RefreshTokens.Update(stored);
            await _db.SaveChangesAsync();

            var userId = principal.FindFirst("userId")?.Value;
            if (string.IsNullOrEmpty(userId))
                return Result.Failure<TokenPair>("userId не найден в токене");

            var user = await _db.Users.FirstOrDefaultAsync(u => u.Id == userId);
            if (user is null)
                return Result.Failure<TokenPair>("Пользователь не найден");

            var userResponse = new UserResponse(
                user.Id,
                user.Name,
                user.Email,
                null,
                null,
                user.CreatedAt,
                new List<UserShabashParticipationResponse>()
            );

            return await GenerateTokens(userResponse);
        }
        public async Task RevokeAllUserTokens(string userId)
        {
            await _db.RefreshTokens
                .Where(r => r.UserId == userId && !r.IsRevoked)
                .ExecuteUpdateAsync(s => s.SetProperty(r => r.IsRevoked, true));
        }

        private static string GenerateRandomToken()
        {
            var bytes = RandomNumberGenerator.GetBytes(64);
            return Convert.ToBase64String(bytes);
        }
    }
}