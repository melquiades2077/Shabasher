using CSharpFunctionalExtensions;
using Shabasher.Core.Validators;

namespace Shabasher.Core.Models
{
    public class Shabash
    {
        public string Id { get; }

        public string Name { get; }

        public string Description { get; } = "";

        public List<User> Participants { get; } = [];

        public DateTime CreatedAt { get; }

        private Shabash(string id, string name, string description, List<User> participants)
        {
            Id = id;
            Name = name;
            Description = description;
            Participants = participants;
            CreatedAt = DateTime.UtcNow;
        }

        public static Result<Shabash> Create(string name, string description, List<User> participants)
        {
            var validationResult = ShabashValidator.ValidateShabashCreation(name, description);
            if (validationResult.IsFailure)
                return Result.Failure<Shabash>(validationResult.Error);

            string id = Guid.NewGuid().ToString();

            return Result.Success<Shabash>(new Shabash(id, name, description, participants));
        }
    }
}
