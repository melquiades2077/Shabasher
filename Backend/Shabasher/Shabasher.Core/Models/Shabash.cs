using CSharpFunctionalExtensions;
using Shabasher.Core.Validators;

namespace Shabasher.Core.Models
{
    public class Shabash
    {
        public string Id { get; }

        public string Name { get; }

        public string Description { get; } = "";

        public List<ShabashParticipant> Participants { get; } = [];

        public DateTime StartDate { get; }

        public DateTime CreatedAt { get; }

        public ShabashStatus Status =>
            DateTime.UtcNow >= StartDate ? ShabashStatus.Finished : ShabashStatus.Active;

        private Shabash(string id, string name, string description, DateTime startDate, List<ShabashParticipant> participants)
        {
            Id = id;
            Name = name;
            Description = description;
            Participants = participants;
            StartDate = startDate;
            CreatedAt = DateTime.UtcNow;
        }

        public static Result<Shabash> Create(string name, string description, DateTime startDate, List<ShabashParticipant> participants)
        {
            var validationResult = ShabashValidator.ValidateShabashCreation(name, description, startDate);
            if (validationResult.IsFailure)
                return Result.Failure<Shabash>(validationResult.Error);

            string id = Guid.NewGuid().ToString();

            return Result.Success<Shabash>(new Shabash(id, name, description, startDate, participants));
        }
    }
}
