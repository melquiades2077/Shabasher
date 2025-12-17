using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class ShabashValidator
    {
        private const int MAX_DESCRIPTION_LEN = 600;
        private const int MAX_ADDRESS_LEN = 120;

        public static Result ValidateShabashCreation(string name, string description, string address, DateTime startDate)
        {
            var errors = new List<string>();

            var nameResult = NameValidator.IsValidName(name);
            if (nameResult.IsFailure)
                errors.Add(nameResult.Error);

            if (description.Length > MAX_DESCRIPTION_LEN)
                errors.Add($"Длина описания события не должна превышать {MAX_DESCRIPTION_LEN}");

            if (address.Length > MAX_ADDRESS_LEN)
                errors.Add($"Длина адреса события не должна превышать {MAX_ADDRESS_LEN}");

            if (startDate <= DateTime.UtcNow)
                errors.Add("Дата начала события должна быть в будущем");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
