using System.Text.RegularExpressions;
using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class EmailValidator
    {
        private const int MAX_EMAIL_LEN = 254;

        public static Result IsValidEmail(string email)
        {
            var errors = new List<string>();

            if (string.IsNullOrWhiteSpace(email))
                errors.Add("Электронная почта обязательна для заполнения");

            if (email.Length > MAX_EMAIL_LEN)
                errors.Add($"Длина электронной почты не должна превышать {MAX_EMAIL_LEN}");

            try
            {
                if (!Regex.IsMatch(
                    email,
                    @"(?<Login>[a-zA-Z0-9._%+-]+)@(?<Domain>[a-zA-Z0-9.-]+)\.(?<HLDomain>[a-zA-Z]{2,})\b",
                    RegexOptions.IgnoreCase,
                    TimeSpan.FromMilliseconds(250)
                ))
                    errors.Add("Неверный формат электронной почты");
            }
            catch (RegexMatchTimeoutException)
            {
                errors.Add("Неверный формат электронной почты");
            }

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
