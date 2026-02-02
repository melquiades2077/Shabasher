using CSharpFunctionalExtensions;
namespace Shabasher.Core.Validators
{
    public static class TelegramValidator
    {
        private const int MAX_TELEGRAM_LEN = 32;
        private const int MIN_TELEGRAM_LEN = 5;

        public static Result IsValidTelegram(string telegram)
        {
            var errors = new List<string>();

            if (telegram.Length < MIN_TELEGRAM_LEN)
                errors.Add($"Имя телеграм-аккаунта должно быть не меньше {MIN_TELEGRAM_LEN} символов");

            if (telegram.Length > MAX_TELEGRAM_LEN)
                errors.Add($"Имя телеграм-аккаунта не должно превышать {MAX_TELEGRAM_LEN} символов");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
