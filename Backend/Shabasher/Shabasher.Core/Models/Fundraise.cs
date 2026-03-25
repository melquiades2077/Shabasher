using CSharpFunctionalExtensions;
using Microsoft.AspNetCore.Identity;
using Shabasher.Core.Validators;

namespace Shabasher.Core.Models
{
    public class Fundraise
    {
        public string Id { get; }
        public string Name { get; }
        public string EventId { get; }
        public string CreatorId { get; }
        public string CreatorPhone { get; }
        //имя получателя по карточке
        public string CreatorName { get; }
        public string? Description { get; }
        public decimal? TargetAmount { get; }
        public decimal CurrentAmount { get; }
        public FundStatus FundStatus { get; }
        public DateTime CreatedAt { get; }

        private Fundraise(string name, string eventId, 
            string creatorId, string creatorPhone, string creatorName, 
            string description, decimal targetAmount)
        {
            Id = Guid.NewGuid().ToString();
            Name = name;
            EventId = eventId;
            CreatorId = creatorId;
            CreatorPhone = creatorPhone;
            CreatorName = creatorName;
            Description = description;
            TargetAmount = targetAmount;
            CurrentAmount = 0;
            FundStatus = FundStatus.Active;
            CreatedAt = DateTime.UtcNow;
        }

        public static Result<Fundraise> Create(string name, string eventId,
                                               string creatorId, string creatorPhone, string creatorName,
                                               string description, decimal targetAmount)
        {
            var validationResult = FundraiseValidator.ValidateFundraiseCreation(name, creatorPhone, creatorName, description, targetAmount);
            if (validationResult.IsFailure)
                return Result.Failure<Fundraise>(validationResult.Error);

            return Result.Success<Fundraise>(new Fundraise(name, eventId,
                                                           creatorId, creatorPhone, creatorName,
                                                           description, targetAmount));
        }
    }
}
