using CSharpFunctionalExtensions;
using Microsoft.EntityFrameworkCore;
using Shabasher.API.DTOs;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage.Mappings;
using Shabasher.DataManage;
using Shabasher.BusinessLogic.Mappings;

namespace Shabasher.BusinessLogic.Services
{
    //очень тяжёлый класс
    public class UsersManageService : IUsersManageService
    {
        //работа напрямую с контекстом без репозитория
        private readonly ShabasherDbContext _dbcontext;
        private readonly IPasswordHasher _passwordHasher;

        public UsersManageService(ShabasherDbContext dbContext, IPasswordHasher passwordHasher)
        {
            _dbcontext = dbContext;
            _passwordHasher = passwordHasher;
        }

        public async Task<Result<UserResponse>> CreateUserAsync(string name, string email, string password)
        {
            if (await _dbcontext.Users.AnyAsync(u => u.Email == email.ToLower()))
                return Result.Failure<UserResponse>("Пользователь с этим email уже существует");

            var user = User.Create(name, email, password, _passwordHasher);

            _dbcontext.Users.Add(UserEntityMapper.ToEntity(user.Value));
            await _dbcontext.SaveChangesAsync();

            return Result.Success<UserResponse>(UserResponseMapper.DomainToResponse(user.Value));
        }

        public async Task<Result<UserResponse>> GetUserByIdAsync(string id)
        {
            if (string.IsNullOrWhiteSpace(id))
                return Result.Failure<UserResponse>("Необходимо ввести ID пользователя");
            
            var userEntity = await _dbcontext.Users
                .AsNoTracking()
                .FirstOrDefaultAsync(u => u.Id == id);

            if (userEntity == null)
                return Result.Failure<UserResponse>("Пользователь с данным ID не найден");

            return Result.Success<UserResponse>(UserResponseMapper.EntityToResponse(userEntity));
        }

        public async Task<Result<UserResponse>> GetUserByEmailAsync(string email)
        {
            if (string.IsNullOrWhiteSpace(email))
                return Result.Failure<UserResponse>("Необходимо ввести email пользователя");

            var userEntity = await _dbcontext.Users
                .AsNoTracking()
                .FirstOrDefaultAsync(u => u.Email == email);

            if (userEntity == null)
                return Result.Failure<UserResponse>("Пользователь с данным email не найден");

            return Result.Success<UserResponse>(UserResponseMapper.EntityToResponse(userEntity));
        }
    }
}
