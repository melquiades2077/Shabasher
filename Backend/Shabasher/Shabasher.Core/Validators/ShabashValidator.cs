using CSharpFunctionalExtensions;
using Shabasher.Core.Models;

namespace Shabasher.Core.Validators
{
    public static class ShabashValidator
    {
        private const int MAX_NAME_LEN = 100;
        private const int MAX_DESCRIPTION_LEN = 600;

        public static Result ValidateShabashCreation(string name, string description)
        {
            var errors = new List<string>();

            if (string.IsNullOrWhiteSpace(name))
                errors.Add("Название обязательно для заполнения");

            if (name.Length > MAX_NAME_LEN)
                errors.Add($"Название не должно превышать {MAX_NAME_LEN} символов");

            if (description.Length > MAX_DESCRIPTION_LEN)
                errors.Add($"Длина описания события не должна превышать {MAX_DESCRIPTION_LEN}");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
