using CSharpFunctionalExtensions;

namespace Shabasher.Core.Interfaces
{
    public interface IPasswordHasher
    {
        string HashPassword(string password);
        Result VerifyPassword(string password, string hash);
    }
}
