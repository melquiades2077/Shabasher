using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;
using Shabasher.Core.Models;

namespace Shabasher.Core.Interfaces
{
    public interface IJwtProvider
    {
        Task<Result<TokenPair>> GenerateTokens(UserResponse user);
        Task<Result<TokenPair>> RefreshTokens(string accessToken, string refreshToken);
        Task RevokeAllUserTokens(string userId);
    }
}
