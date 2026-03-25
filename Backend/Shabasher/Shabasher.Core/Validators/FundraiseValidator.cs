using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class FundraiseValidator
    {
        private const int MAX_DESCRIPTION_LEN = 600;
        private const decimal MIN_TARGET_AMOUNT = 10;
        public static Result ValidateFundraiseCreation(string name, string creatorPhone, string creatorName,
                                                       string? description, decimal targetAmount)
        {
            var errors = new List<string>();

            var nameResult = NameValidator.IsValidName(name);
            if (nameResult.IsFailure)
                errors.Add(nameResult.Error);

            var phoneResult = PhoneValidator.ValidatePhone(creatorPhone);
            if (phoneResult.IsFailure)
                errors.Add(phoneResult.Error);

            var creatorNameResult = NameValidator.IsValidName(creatorName);
            if (creatorNameResult.IsFailure)
                errors.Add(creatorNameResult.Error);

            if (description?.Length > MAX_DESCRIPTION_LEN)
                errors.Add($"Длина описания сбора не должна превышать {MAX_DESCRIPTION_LEN}");

            if (targetAmount < 10)
                errors.Add($"Минимальная сумма сбора должна быть не меньше {MIN_TARGET_AMOUNT}");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
