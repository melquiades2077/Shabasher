using CSharpFunctionalExtensions;
using Shabasher.Core.Interfaces;

namespace Shabasher.Core
{
    public class PasswordHasher : IPasswordHasher
    {
        public string Generate(string password) =>
            BCrypt.Net.BCrypt.EnhancedHashPassword(password);
        public bool VerifyPassword(string password, string hash) =>
            BCrypt.Net.BCrypt.EnhancedVerify(password, hash);
    }
}
