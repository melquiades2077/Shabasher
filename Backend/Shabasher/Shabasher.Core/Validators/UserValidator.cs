using CSharpFunctionalExtensions;

namespace Shabasher.Core.Validators
{
    public static class UserValidator
    {
        public static Result ValidateUserCreation(string name, string email, string password)
        {
            var errors = new List<string>();

            var nameResult = UserNameValidator.IsValidUserName(name);
            if (nameResult.IsFailure)
                errors.Add(nameResult.Error);

            var emailResult = EmailValidator.IsValidEmail(email);
            if (emailResult.IsFailure)
                errors.Add(emailResult.Error);

            var passwordResult = PasswordValidator.IsValidPassword(password);
            if (passwordResult.IsFailure)
                errors.Add(passwordResult.Error);

            return errors.Any()
                ? Result.Failure(string.Join("; ", errors))
                : Result.Success();
        }
    }
}
