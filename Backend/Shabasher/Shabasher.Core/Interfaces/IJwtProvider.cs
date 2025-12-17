using Shabasher.Core.DTOs;

namespace Shabasher.Core.Interfaces
{
    public interface IJwtProvider
    {
        string GenerateToken(UserResponse user);
    }
}
