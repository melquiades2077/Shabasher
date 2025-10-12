using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class UserValidator
    {
        private const int MAX_NAME_LEN = 100;
        private const int MAX_EMAIL_LEN = 254;

        public static Result ValidateUserCreation(string name, string email)
        {
            var errors = new List<string>();

            if (string.IsNullOrWhiteSpace(name))
                errors.Add("Имя обязательно для заполнения");

            if (name.Length > MAX_NAME_LEN)
                errors.Add($"Имя не должно превышать {MAX_NAME_LEN} символов");

            if (string.IsNullOrWhiteSpace(email))
                errors.Add("Электронная почта обязательна для заполнения");

            if (email.Length > MAX_EMAIL_LEN)
                errors.Add($"Длина электронной почты не должна превышать {MAX_EMAIL_LEN}");

            if (!EmailValidator.IsValidEmail(email))
                errors.Add("Неверный формат электронной почты");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
