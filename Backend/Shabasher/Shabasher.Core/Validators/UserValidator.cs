using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class UserValidator
    {
        private const int MAX_NAME_LEN = 100;
        private const int MAX_EMAIL_LEN = 254;
        private const int MAX_PASSWORD_LEN = 64;
        private const int MIN_PASSWORD_LEN = 8;

        public static Result ValidateUserCreation(string name, string email, string password)
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

            if (string.IsNullOrWhiteSpace(password) || password.Length < MIN_PASSWORD_LEN)
                errors.Add($"Пароль должен содержать минимум {MIN_PASSWORD_LEN} символов");

            if (password.Length > MAX_PASSWORD_LEN)
                errors.Add($"Длина пароля не должна превышать {MAX_PASSWORD_LEN}");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
