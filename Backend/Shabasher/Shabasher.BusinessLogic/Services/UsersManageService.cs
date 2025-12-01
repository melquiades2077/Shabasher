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
        private readonly IJwtProvider _jwtProvider;

        public UsersManageService(ShabasherDbContext dbContext, IPasswordHasher passwordHasher, IJwtProvider jwtProvider)
        {
            _dbcontext = dbContext;
            _passwordHasher = passwordHasher;
            _jwtProvider = jwtProvider;
        }

        public async Task<Result<UserResponse>> RegisterUserAsync(string name, string email, string password)
        {
            if (await _dbcontext.Users.AnyAsync(x => x.Email == email))
                return Result.Failure<UserResponse>("Пользователь с этим email уже существует");

            var user = User.Create(name, email, password, _passwordHasher);

            if (user.IsFailure)
                return Result.Failure<UserResponse>(user.Error);

            _dbcontext.Users.Add(UserEntityMapper.ToEntity(user.Value));
            await _dbcontext.SaveChangesAsync();

            return Result.Success<UserResponse>(UserResponseMapper.DomainToResponse(user.Value));
        }

        public async Task<Result<string>> LoginUserAsync(string email, string password)
        {
            var user = await _dbcontext.Users
                .AsNoTracking()
                .FirstOrDefaultAsync(u => u.Email == email);

            if (user == null)
                return Result.Failure<string>("Пользователь с данным email не найден");

            if (!_passwordHasher.VerifyPassword(password, user.PasswordHash))
                return Result.Failure<string>("Неверный пароль");

            string jwtToken = _jwtProvider.GenerateToken(UserResponseMapper.EntityToResponse(user));

            return Result.Success<string>(jwtToken);
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

        public async Task<Result<string>> DeleteUserAsync(string userId)
        {
            var userEntity = await _dbcontext.Users
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (userEntity == null)
                return Result.Failure<string>("Пользователь с данным ID не найден");

            _dbcontext.Users.Remove(userEntity);
            await _dbcontext.SaveChangesAsync();

            return Result.Success<string>(userEntity.Id);
        }

        public async Task<Result<string>> UpdateUserNameAsync(string userId, string newName)
        {
            var newNameResult = NameValidator.IsValidName(newName);
            if (newNameResult.IsFailure)
                return Result.Failure<string>(newNameResult.Error);
            
            var user = await _dbcontext.Users
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
                return Result.Failure<string>("Пользователь не найден");

            user.Name = newName;
            
            await _dbcontext.SaveChangesAsync();

            return Result.Success<string>(user.Name);
        }
    }
}
