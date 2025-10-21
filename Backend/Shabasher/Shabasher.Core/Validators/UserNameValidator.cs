using CSharpFunctionalExtensions;
using System.Text.RegularExpressions;
using System.Xml.Linq;

namespace Shabasher.Core.Validators
{
    public static class UserNameValidator
    {
        private const int MAX_NAME_LEN = 100;

        public static Result IsValidUserName(string userName)
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
