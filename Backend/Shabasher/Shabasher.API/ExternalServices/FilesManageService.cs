using Amazon.S3;
using Amazon.S3.Model;
using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;

namespace Shabasher.API.Services
{
    public class FilesManageService : IFilesManageService
    {
        private readonly IAmazonS3 _s3;
        private readonly string _bucketName;
        private readonly string _publicBaseUrl;

        public FilesManageService(IAmazonS3 s3, IConfiguration configuration)
        {
            _s3 = s3;
            _bucketName = Environment.GetEnvironmentVariable("S3_BUCKET")
                ?? configuration["S3_BUCKET"]
                ?? string.Empty;
            // Базовый URL приложения (через nginx). Файлы отдаются прокси-эндпоинтом /api/files/{key}.
            _publicBaseUrl = (Environment.GetEnvironmentVariable("BASE_URL")
                ?? configuration["BASE_URL"]
                ?? string.Empty).TrimEnd('/');
        }

        public async Task<Result<FileUploadResult>> UploadImageAsync(
            Stream stream,
            string fileName,
            string contentType,
            string folderPrefix,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(_bucketName))
                return Result.Failure<FileUploadResult>("S3 bucket is not configured");

            var extension = Path.GetExtension(fileName);
            var objectKey = $"{folderPrefix.Trim('/')}/{Guid.NewGuid()}{extension}";

            var request = new PutObjectRequest
            {
                BucketName = _bucketName,
                Key = objectKey,
                InputStream = stream,
                ContentType = contentType,
                AutoCloseStream = false,
                CannedACL = S3CannedACL.PublicRead
            };

            try
            {
                await _s3.PutObjectAsync(request, cancellationToken);

                var url = string.IsNullOrWhiteSpace(_publicBaseUrl)
                    ? $"/api/files/{objectKey}"
                    : $"{_publicBaseUrl}/api/files/{objectKey}";

                return Result.Success(new FileUploadResult(objectKey, url));
            }
            catch (Exception ex)
            {
                return Result.Failure<FileUploadResult>($"S3 upload failed: {ex.Message}");
            }
        }

        public async Task<Result> DeleteFileAsync(string objectKey, CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(_bucketName))
                return Result.Failure("S3 bucket is not configured");
            if (string.IsNullOrWhiteSpace(objectKey))
                return Result.Success();

            try
            {
                await _s3.DeleteObjectAsync(new DeleteObjectRequest
                {
                    BucketName = _bucketName,
                    Key = objectKey
                }, cancellationToken);

                return Result.Success();
            }
            catch (Exception ex)
            {
                return Result.Failure($"S3 delete failed: {ex.Message}");
            }
        }
    }
}

