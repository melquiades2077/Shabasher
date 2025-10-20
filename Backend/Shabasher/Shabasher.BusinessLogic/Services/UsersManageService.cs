using CSharpFunctionalExtensions;
using Microsoft.EntityFrameworkCore;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage;
using Shabasher.DataManage.Entities;

namespace Shabasher.BusinessLogic.Services
{
    public class UsersManageService : IUsersManageService
    {
        //работа напрямую с контекстом без репозитория
        private readonly ShabasherDbContext _dbcontext;

        public UsersManageService(ShabasherDbContext dbContext)
        {
            _dbcontext = dbContext;
        }

        public async Task<Result<string>> CreateUserAsync(string name, string email, string password)
        {

        }
    }
}
