using Shabasher.DataManage;
using Shabasher.Core.Interfaces;

namespace Shabasher.BusinessLogic.Services
{
    public class ShabashesService : IShabashesService
    {
        private readonly ShabasherDbContext _dbcontext;

        public ShabashesService(ShabasherDbContext dbContext)
        {
            _dbcontext = dbContext;
        }
    }
}
