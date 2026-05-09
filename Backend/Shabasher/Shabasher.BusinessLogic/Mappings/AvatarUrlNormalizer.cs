namespace Shabasher.BusinessLogic.Mappings
{
    /// <summary>
    /// Возвращает публичную ссылку на аватар по objectKey (или AvatarUrl,
    /// если в БД лежит уже готовая ссылка).
    /// Файлы отдаются прокси-эндпоинтом /api/files/{key}, чтобы не зависеть
    /// от ACL/формата S3-URL'ов провайдера.
    /// </summary>
    public static class AvatarUrlNormalizer
    {
        public static string? BuildUrl(string? avatarObjectKey, string? legacyAvatarUrl)
        {
            // Приоритет — objectKey, он гарантированно валиден.
            if (!string.IsNullOrWhiteSpace(avatarObjectKey))
                return BuildFromKey(avatarObjectKey.TrimStart('/'));

            // Legacy: до фикса в БД могло остаться значение AvatarUrl без objectKey.
            if (string.IsNullOrWhiteSpace(legacyAvatarUrl))
                return null;

            var trimmed = legacyAvatarUrl.Trim();

            // Если уже сохранена ссылка через прокси — отдаём как есть.
            if (trimmed.Contains("/api/files/", System.StringComparison.OrdinalIgnoreCase))
                return trimmed;

            // Если в БД лежит абсолютный URL (старый S3-формат) — пытаемся
            // вытащить objectKey после имени бакета.
            if (trimmed.StartsWith("http://", System.StringComparison.OrdinalIgnoreCase) ||
                trimmed.StartsWith("https://", System.StringComparison.OrdinalIgnoreCase))
            {
                var bucket = (System.Environment.GetEnvironmentVariable("S3_BUCKET") ?? string.Empty).Trim('/');
                if (!string.IsNullOrEmpty(bucket))
                {
                    var marker = $"/{bucket}/";
                    var idx = trimmed.IndexOf(marker, System.StringComparison.OrdinalIgnoreCase);
                    if (idx >= 0)
                    {
                        var key = trimmed.Substring(idx + marker.Length);
                        return BuildFromKey(key.TrimStart('/'));
                    }
                }
                return trimmed; // не угадали структуру — пусть клиент попробует
            }

            // Чистый objectKey без http
            return BuildFromKey(trimmed.TrimStart('/'));
        }

        private static string BuildFromKey(string key)
        {
            var baseUrl = (System.Environment.GetEnvironmentVariable("BASE_URL") ?? string.Empty).TrimEnd('/');
            return string.IsNullOrEmpty(baseUrl)
                ? $"/api/files/{key}"
                : $"{baseUrl}/api/files/{key}";
        }
    }
}
