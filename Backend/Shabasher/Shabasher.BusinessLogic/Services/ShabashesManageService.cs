using Shabasher.DataManage;
using Shabasher.Core.Interfaces;

namespace Shabasher.BusinessLogic.Services
{
    public class ShabashesManageService : IShabashesManageService
    {
        private readonly ShabasherDbContext _dbcontext;

        public ShabashesManageService(ShabasherDbContext dbContext)
        {
            _dbcontext = dbContext;
        }
    }
}
