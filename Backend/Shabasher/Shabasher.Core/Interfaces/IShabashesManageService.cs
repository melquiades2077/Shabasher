using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;
using Shabasher.Core.Models;

namespace Shabasher.Core.Interfaces
{
    public interface IShabashesManageService
    {
        Task<Result<string>> CreateShabashAsync(string name, string description, string address, DateTime startDate, string creatorUserId, List<ShabashParticipant> participants);
        Task<Result<ShabashResponse>> UpdateShabashAsync(string shabashId, UpdateShabashRequest request, string userId);
        Task<Result<string>> DeleteShabashAsync(string shabashId, string userId);
        Task<Result<ShabashResponse>> GetShabashByIdAsync(string shabashId);
    }
}
