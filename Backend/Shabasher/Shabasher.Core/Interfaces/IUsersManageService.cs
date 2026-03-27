using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;
using Shabasher.Core.Models;

namespace Shabasher.Core.Interfaces
{
    public interface IUsersManageService
    {
        Task<Result<UserResponse>> RegisterUserAsync(string name, string email, string password, string? aboutMe = null, string? telegram = null);
        Task<Result<string>> LoginUserAsync(string email, string password);
        Task<Result<UserResponse>> GetUserByIdAsync(string id);
        Task<Result<UserResponse>> GetUserByEmailAsync(string email);
        Task<Result<UserResponse>> UpdateUserProfileAsync(string userId, string newName, string? aboutMe, string? telegram);
        Task<Result<string>> DeleteUserAsync(string userId);
        Task<Result<string>> UpdatePastorStatusAsync(string userId, string shabashId, UserStatus status);
    }
}
