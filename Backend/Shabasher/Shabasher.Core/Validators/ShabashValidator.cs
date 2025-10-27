using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class ShabashValidator
    {
        private const int MAX_DESCRIPTION_LEN = 600;

        public static Result ValidateShabashCreation(string name, string description)
        {
            var errors = new List<string>();

            var nameResult = NameValidator.IsValidName(name);
            if (nameResult.IsFailure)
                errors.Add(nameResult.Error);

            if (description.Length > MAX_DESCRIPTION_LEN)
                errors.Add($"Длина описания события не должна превышать {MAX_DESCRIPTION_LEN}");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
