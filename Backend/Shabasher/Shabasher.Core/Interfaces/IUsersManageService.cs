using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;

namespace Shabasher.Core.Interfaces
{
    public interface IUsersManageService
    {
        Task<Result<UserResponse>> RegisterUserAsync(string name, string email, string password);
        Task<Result<string>> LoginUserAsync(string email, string password);
        Task<Result<UserResponse>> GetUserByIdAsync(string id);
        Task<Result<UserResponse>> GetUserByEmailAsync(string email);
        Task<Result> UpdateUserNameAsync(string userId, string newName);
        Task<Result> DeleteUserAsync(string userId);
    }
}
