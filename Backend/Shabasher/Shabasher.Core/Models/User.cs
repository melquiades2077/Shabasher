using CSharpFunctionalExtensions;
using Shabasher.Core.Validators;

namespace Shabasher.Core.Models
{
    public class User
    {
        public string Id { get; }

        public string Name { get; }

        public string Email { get; }

        public DateTime CreatedAt { get; }

        private string _password;

        private User(string id, string name, string email, string password)
        {
            Id = id;
            Name = name;
            Email = email;
            _password = password;
            CreatedAt = DateTime.UtcNow;
        }

        public static Result<User> Create(string name, string email, string password)
        {
            var validationResult = UserValidator.ValidateUserCreation(name, email);
            if (validationResult.IsFailure)
                return Result.Failure<User>(validationResult.Error);

            string id = Guid.NewGuid().ToString();

            return Result.Success<User>(new User(id, name, email, password));
        }
    }

}
