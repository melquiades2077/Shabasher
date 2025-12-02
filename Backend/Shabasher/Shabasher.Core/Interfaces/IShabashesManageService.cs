using CSharpFunctionalExtensions;
using Shabasher.Core.DTOs;
using Shabasher.Core.Models;

namespace Shabasher.Core.Interfaces
{
    public interface IShabashesManageService
    {
        Task<Result<string>> CreateShabashAsync(string name, string description, DateTime startDate, List<ShabashParticipant> participants);
        Task<Result<ShabashResponse>> UpdateShabashAsync(string shabashId, UpdateShabashRequest request, string userId);
    }
}
