using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public class PasswordValidator
    {
        private const int MAX_PASSWORD_LEN = 64;
        private const int MIN_PASSWORD_LEN = 8;

        public static Result IsValidPassword(string password)
        {
            var errors = new List<string>();

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
