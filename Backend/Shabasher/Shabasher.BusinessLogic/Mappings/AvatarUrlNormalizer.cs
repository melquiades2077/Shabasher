namespace Shabasher.BusinessLogic.Mappings
{
    /// <summary>
    /// Превращает сохранённое в БД значение AvatarUrl в публичную ссылку.
    /// Если в базе уже лежит абсолютный URL (http/https) — возвращает как есть.
    /// Если только objectKey — склеивает с S3_PUBLIC_BASE_URL/ENDPOINT и именем бакета.
    /// </summary>
    public static class AvatarUrlNormalizer
    {
        public static string? Normalize(string? value)
        {
            if (string.IsNullOrWhiteSpace(value))
                return null;

            var trimmed = value.Trim();
            if (trimmed.StartsWith("http://", System.StringComparison.OrdinalIgnoreCase) ||
                trimmed.StartsWith("https://", System.StringComparison.OrdinalIgnoreCase))
            {
                return trimmed;
            }

            var publicBase = (System.Environment.GetEnvironmentVariable("S3_PUBLIC_BASE_URL")
                ?? System.Environment.GetEnvironmentVariable("ENDPOINT")
                ?? string.Empty).TrimEnd('/');

            var bucket = (System.Environment.GetEnvironmentVariable("S3_BUCKET") ?? string.Empty).Trim('/');

            if (string.IsNullOrEmpty(publicBase))
                return trimmed;

            var key = trimmed.TrimStart('/');
            return string.IsNullOrEmpty(bucket)
                ? $"{publicBase}/{key}"
                : $"{publicBase}/{bucket}/{key}";
        }
    }
}
