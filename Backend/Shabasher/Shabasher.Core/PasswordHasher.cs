using CSharpFunctionalExtensions;
using Shabasher.Core.Interfaces;

namespace Shabasher.Core
{
    public class PasswordHasher : IPasswordHasher
    {
        public string HashPassword(string password)
        {
            return password;
        }
        public Result VerifyPassword(string password, string hash)
        {
            return hash == password ? Result.Success() : Result.Failure("Пароли не совпадают");
        }
    }
}
