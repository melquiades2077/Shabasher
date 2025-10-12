using System.Text.RegularExpressions;

namespace Shabasher.Core.Validators
{
    public static class EmailValidator
    {
        public static bool IsValidEmail(string email)
        {
            try
            {
                return Regex.IsMatch(
                    email,
                    @"(?<Login>[a-zA-Z0-9._%+-]+)@(?<Domain>[a-zA-Z0-9.-]+)\.(?<HLDomain>[a-zA-Z]{2,})\b",
                    RegexOptions.IgnoreCase,
                    TimeSpan.FromMilliseconds(250)
                );
            }
            catch (RegexMatchTimeoutException)
            {
                return false;
            }
        }
    }
}
