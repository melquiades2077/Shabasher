using CSharpFunctionalExtensions;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Validators;

namespace Shabasher.Core.Models
{
    public class User
    {
        public string Id { get; }

        public string Name { get; }

        public string Email { get; }

        public string AboutMe { get; } = "";

        public string Telegram { get; } = "";

        public DateTime CreatedAt { get; }

        public List<Shabash> Shabashes { get; }

        public string PasswordHash { get; }

        private User(string id, string name, string email, string about, string telegram, string passwordHash)
        {
            Id = id;
            Name = name;
            Email = email;
            AboutMe = about;
            Telegram = telegram;
            PasswordHash = passwordHash;
            CreatedAt = DateTime.UtcNow;
            Shabashes = [];
        }

        private User(string id, string name, string email, string about, string telegram, string passwordHash, DateTime createdAt)
        {
            Id = id;
            Name = name;
            Email = email;
            AboutMe = about;
            Telegram = telegram;
            PasswordHash = passwordHash;
            CreatedAt = createdAt;
            Shabashes = [];
        }

        public static Result<User> Create(string name, string email, string about, string telegram, string password, IPasswordHasher passwordHasher)
        {
            var validationResult = UserValidator.ValidateUserCreation(name, email, telegram, about, password);
            var passwordHash = passwordHasher.Generate(password);
            if (validationResult.IsFailure)
                return Result.Failure<User>(validationResult.Error);

            string id = Guid.NewGuid().ToString();

            return Result.Success<User>(new User(id, name, email, about, telegram, passwordHash));
        }

        public static User FromEntity(string id,
                string name,
                string email,
                string about, 
                string telegram,
                string passwordHash,
                DateTime createdAt) => new User(id, name, email, about, telegram, passwordHash, createdAt);
    }
}
