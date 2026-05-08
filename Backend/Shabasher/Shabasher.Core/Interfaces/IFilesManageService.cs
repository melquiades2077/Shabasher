using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;

namespace Shabasher.Core.Interfaces
{
    public interface IFilesManageService
    {
        Task<Result<FileUploadResult>> UploadImageAsync(
            Stream stream,
            string fileName,
            string contentType,
            string folderPrefix,
            CancellationToken cancellationToken = default);

        Task<Result> DeleteFileAsync(string objectKey, CancellationToken cancellationToken = default);
    }
}
