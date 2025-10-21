using CSharpFunctionalExtensions;
using Microsoft.EntityFrameworkCore;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage.Mappings;
using Shabasher.DataManage;
using Shabasher.BusinessLogic.Mappings;
using Shabasher.Core.Validators;

namespace Shabasher.BusinessLogic.Services
{
    //тяжёлый класс
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

            if (user.IsFailure)
                return Result.Failure<UserResponse>(user.Error);

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

        public async Task<Result> DeleteUserAsync(string userId)
        {
            //дублирование, потому что хочу чтобы Get возвращал Response. Хочу и делаю
            var userEntity = await _dbcontext.Users
                .AsNoTracking()
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (userEntity == null)
                return Result.Failure<UserResponse>("Пользователь с данным ID не найден");

            _dbcontext.Users.Remove(userEntity);

            return Result.Success(userEntity.Id);
        }

        public async Task<Result> UpdateUserNameAsync(string userId, string newName)
        {
            var newNameResult = UserNameValidator.IsValidUserName(newName);
            if (newNameResult.IsFailure)
                return Result.Failure(newNameResult.Error);
            
            var user = await _dbcontext.Users
                .AsNoTracking()
                .FirstOrDefaultAsync(u => u.Id == userId);

            user.Name = newName;

            return Result.Success(user.Name);
        }
    }
}
