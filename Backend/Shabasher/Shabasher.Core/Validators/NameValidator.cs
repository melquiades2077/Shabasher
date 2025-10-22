using CSharpFunctionalExtensions;
namespace Shabasher.Core.Validators
{
    public static class NameValidator
    {
        private const int MAX_NAME_LEN = 100;

        public static Result IsValidName(string userName)
        {
            var errors = new List<string>();

            if (string.IsNullOrWhiteSpace(userName))
                errors.Add("Имя обязательно для заполнения");

            if (userName.Length > MAX_NAME_LEN)
                errors.Add($"Имя не должно превышать {MAX_NAME_LEN} символов");

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
