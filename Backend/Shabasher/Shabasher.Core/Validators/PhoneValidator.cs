using CSharpFunctionalExtensions;
using System.Text.RegularExpressions;

namespace Shabasher.Core.Validators
{
    public static class PhoneValidator
    {
        private const string ERROR = "Неправильный формат телефона";
        public static Result ValidatePhone(string phone)
        {
            if (string.IsNullOrWhiteSpace(phone))
                return Result.Failure(ERROR);

            if (!phone
                .All(c => char.IsDigit(c) 
                || c == '(' 
                || c == ')' 
                || c == '-' 
                || c == ' ' 
                || c == '+'))
                return Result.Failure(ERROR);
            var digits = Regex.Replace(phone, @"\D", "");

            bool isValid = digits.Length == 10 
                || (digits.Length == 11 && digits[0] == '7') 
                || (digits.Length == 11 && digits[0] == '8');

            return isValid ? Result.Success() : Result.Failure(ERROR);
        }
    }
}
