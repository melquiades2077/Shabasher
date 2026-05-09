using Amazon.S3;
using Amazon.S3.Model;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Shabasher.API.Controllers
{
    /// <summary>
    /// Прокси-эндпоинт для отдачи объектов из S3.
    /// Не требует авторизации (ключи объектов содержат GUID и неугадываемы).
    /// Так Coil/браузер не зависят от публичности бакета и формата S3-URL.
    /// </summary>
    [ApiController]
    [AllowAnonymous]
    [Route("api/files")]
    public class FilesController : ControllerBase
    {
        private readonly IAmazonS3 _s3;
        private readonly string _bucketName;

        public FilesController(IAmazonS3 s3, IConfiguration configuration)
        {
            _s3 = s3;
            _bucketName = Environment.GetEnvironmentVariable("S3_BUCKET")
                ?? configuration["S3_BUCKET"]
                ?? string.Empty;
        }

        // {**objectKey} — catch-all, чтобы пути со слэшами (users/{id}/avatar/{guid}.jpg) ловились целиком.
        [HttpGet("{**objectKey}")]
        public async Task<IActionResult> Get([FromRoute] string objectKey, CancellationToken cancellationToken)
        {
            if (string.IsNullOrWhiteSpace(_bucketName) || string.IsNullOrWhiteSpace(objectKey))
                return NotFound();

            try
            {
                var resp = await _s3.GetObjectAsync(_bucketName, objectKey, cancellationToken);

                Response.Headers.CacheControl = "public, max-age=86400";
                if (!string.IsNullOrWhiteSpace(resp.ETag))
                    Response.Headers.ETag = resp.ETag;

                var contentType = string.IsNullOrWhiteSpace(resp.Headers.ContentType)
                    ? "application/octet-stream"
                    : resp.Headers.ContentType;

                return File(resp.ResponseStream, contentType);
            }
            catch (AmazonS3Exception ex) when (ex.StatusCode == System.Net.HttpStatusCode.NotFound)
            {
                return NotFound();
            }
            catch (AmazonS3Exception)
            {
                return StatusCode(StatusCodes.Status502BadGateway, "Файл недоступен");
            }
        }
    }
}
