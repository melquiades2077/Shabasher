using CSharpFunctionalExtensions;
using Shabasher.Core.Models;

namespace Shabasher.Core.Interfaces
{
    public interface IShabashesManageService
    {
        Task<Result<string>> CreateShabashAsync(string name, string description, List<ShabashParticipant> participants);
    }
}
