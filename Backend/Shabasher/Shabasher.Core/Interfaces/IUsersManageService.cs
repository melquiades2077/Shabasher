using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;
using Shabasher.Core.Models;

namespace Shabasher.Core.Interfaces
{
    public interface IUsersManageService
    {
        Task<Result<UserResponse>> RegisterUserAsync(string name, string email, string password);
        Task<Result<string>> LoginUserAsync(string email, string password);
        Task<Result<UserResponse>> GetUserByIdAsync(string id);
        Task<Result<UserResponse>> GetUserByEmailAsync(string email);
        Task<Result<string>> UpdateUserNameAsync(string userId, string newName);
        Task<Result<string>> DeleteUserAsync(string userId);
        Task<Result<string>> UpdatePastorStatusAsync(string userId, string shabashId, UserStatus status);
    }
}
