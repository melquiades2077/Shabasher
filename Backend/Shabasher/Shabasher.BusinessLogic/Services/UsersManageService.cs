using CSharpFunctionalExtensions;
using Microsoft.EntityFrameworkCore;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using Shabasher.DataManage.Mappings;
using Shabasher.DataManage;
using Shabasher.BusinessLogic.Mappings;
using Shabasher.Core.Validators;
using Shabasher.DataManage.Entities;

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

        private async Task<UserEntity> GetUserEntityByEmail(string email)
        {
            return await _dbcontext.Users
                .AsNoTracking()
                .Include(u => u.Participations)
                .ThenInclude(p => p.Shabash)
                .FirstOrDefaultAsync(u => u.Email == email);
        }

        private async Task<UserEntity> GetUserEntityById(string id)
        {
            return await _dbcontext.Users
                .AsNoTracking()
                .Include(u => u.Participations)
                .ThenInclude(p => p.Shabash)
                .FirstOrDefaultAsync(u => u.Id == id);
        }
        public async Task<Result<UserResponse>> RegisterUserAsync(string name, string email, string password, string? aboutMe = null, string? telegram = null)
        {
            if (await _dbcontext.Users.AnyAsync(x => x.Email == email))
                return Result.Failure<UserResponse>("Пользователь с этим email уже существует");

            var user = User.Create(name, email, aboutMe ?? string.Empty, telegram ?? string.Empty, password, _passwordHasher);

            if (user.IsFailure)
                return Result.Failure<UserResponse>(user.Error);

            _dbcontext.Users.Add(UserEntityMapper.ToEntity(user.Value));
            await _dbcontext.SaveChangesAsync();

            return Result.Success<UserResponse>(UserResponseMapper.DomainToResponse(user.Value));
        }

        public async Task<Result<string>> LoginUserAsync(string email, string password)
        {
            var userEntity = GetUserEntityByEmail(email).Result;

            if (userEntity == null)
                return Result.Failure<string>("Пользователь с данным email не найден");

            if (!_passwordHasher.VerifyPassword(password, userEntity.PasswordHash))
                return Result.Failure<string>("Неверный пароль");

            string jwtToken = _jwtProvider.GenerateToken(UserResponseMapper.EntityToResponse(userEntity));

            return Result.Success<string>(jwtToken);
        }

        public async Task<Result<UserResponse>> GetUserByIdAsync(string id)
        {
            if (string.IsNullOrWhiteSpace(id))
                return Result.Failure<UserResponse>("Необходимо ввести ID пользователя");

            var userEntity = GetUserEntityById(id).Result;

            if (userEntity == null)
                return Result.Failure<UserResponse>("Пользователь с данным ID не найден");

            return Result.Success<UserResponse>(UserResponseMapper.EntityToResponse(userEntity));
        }

        public async Task<Result<UserResponse>> GetUserByEmailAsync(string email)
        {
            if (string.IsNullOrWhiteSpace(email))
                return Result.Failure<UserResponse>("Необходимо ввести email пользователя");

            var userEntity = GetUserEntityByEmail(email).Result;

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

        public async Task<Result<UserResponse>> UpdateUserProfileAsync(string userId, string newName, string? aboutMe, string? telegram)
        {
            using var transaction = await _dbcontext.Database.BeginTransactionAsync();

            try
            {
                var user = await _dbcontext.Users
                    .Include(u => u.Participations)
                    .ThenInclude(p => p.Shabash)
                    .FirstOrDefaultAsync(u => u.Id == userId);

                if (user == null)
                    return Result.Failure<UserResponse>("Пользователь не найден");

                if (newName != null)
                {
                    var newNameResult = NameValidator.IsValidName(newName);
                    if (newNameResult.IsFailure)
                        return Result.Failure<UserResponse>(newNameResult.Error);
                    user.Name = newName;
                }
                if (aboutMe != null)
                {
                    if (aboutMe.Length > 400)
                        return Result.Failure<UserResponse>("Длина секции 'Обо мне' не должна превышать 400 символов");
                    user.AboutMe = aboutMe;
                }
                if (telegram != null)
                {
                    var telegramResult = TelegramValidator.IsValidTelegram(telegram);
                    if (telegramResult.IsFailure)
                        return Result.Failure<UserResponse>(telegramResult.Error);
                    user.Telegram = telegram;
                }

                await _dbcontext.SaveChangesAsync();
                await transaction.CommitAsync();

                return Result.Success<UserResponse>(UserResponseMapper.EntityToResponse(user));
            }
            catch (Exception ex)
            {
                await transaction.RollbackAsync();
                return Result.Failure<UserResponse>($"Ошибка при редактировании профиля: {ex.Message}");
            }
        }

        public async Task<Result<string>> UpdatePastorStatusAsync(string userId, string shabashId, UserStatus status)
        {
            var sp = await _dbcontext.ShabashParticipants.FirstOrDefaultAsync(p => p.UserId == userId && p.ShabashId == shabashId);

            if (sp == null)
                return Result.Failure<string>("Участник шабаша не найден");

            sp.Status = status;
            await _dbcontext.SaveChangesAsync();

            return sp.UserId;
        }
    }
}
