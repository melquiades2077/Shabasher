using CSharpFunctionalExtensions;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage;
using Shabasher.DataManage.Mappings;

namespace Shabasher.BusinessLogic.Services
{
    public class ShabashesManageService : IShabashesManageService
    {
        private readonly ShabasherDbContext _dbcontext;

        public ShabashesManageService(ShabasherDbContext dbContext)
        {
            _dbcontext = dbContext;
        }

        public async Task<Result<string>> CreateShabashAsync(string name, string description, List<User> participants)
        {
            var shabash = Shabash.Create(name, description, participants);

            if (shabash.IsFailure)
                return Result.Failure<string>(shabash.Error);

            _dbcontext.Shabashes.Add(ShabashEntityMapper.ToEntity(shabash.Value));
            await _dbcontext.SaveChangesAsync();

            return Result.Success<string>(shabash.Value.Id);
        }
    }
}
