using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class UserValidator
    {
        private const int MAX_ABOUT_SIZE = 400;
        public static Result ValidateUserCreation(string name, string email, string? telegram, string? about, string password)
        {
            var errors = new List<string>();

            var nameResult = NameValidator.IsValidName(name);
            if (nameResult.IsFailure)
                errors.Add(nameResult.Error);

            var emailResult = EmailValidator.IsValidEmail(email);
            if (emailResult.IsFailure)
                errors.Add(emailResult.Error);

            if (!string.IsNullOrWhiteSpace(telegram) && telegram.Trim() != "@")
            {
                var telegramResult = TelegramValidator.IsValidTelegram(telegram);
                if (telegramResult.IsFailure)
                    errors.Add(telegramResult.Error);
            }

            if (!string.IsNullOrEmpty(about) && about.Length > MAX_ABOUT_SIZE)
                errors.Add($"Длина секции 'Обо мне' не должна превышать {MAX_ABOUT_SIZE} символов");

            var passwordResult = PasswordValidator.IsValidPassword(password);
            if (passwordResult.IsFailure)
                errors.Add(passwordResult.Error);

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
