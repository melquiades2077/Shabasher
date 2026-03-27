using Microsoft.AspNetCore.Identity.Data;
using Microsoft.AspNetCore.Mvc;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;

namespace Shabasher.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly IUsersManageService _usersManageService;
        private readonly IJwtProvider _jwtProvider;

        public AuthController(IUsersManageService usersManageService, IJwtProvider jwtProvider)
        {
            _usersManageService = usersManageService;
            _jwtProvider = jwtProvider;

        }

        [HttpPost("register")]
        public async Task<ActionResult<UserResponse>> RegisterUser([FromBody] RegisterUserRequest registerUserRequest)
        {
            var user = await _usersManageService.RegisterUserAsync(
                registerUserRequest.Name,
                registerUserRequest.Email,
                registerUserRequest.Password,
                registerUserRequest.AboutMe,
                registerUserRequest.Telegram
                );

            if (user.IsFailure)
                return BadRequest(user.Error);

            return Ok(user.Value);
        }

        [HttpPost("login")]
        public async Task<ActionResult<string>> LoginUser([FromBody] LoginUserRequest loginUserRequest)
        {
            var token = await _usersManageService.LoginUserAsync(
                loginUserRequest.Email,
                loginUserRequest.Password
                );

            if (token.IsFailure)
                return BadRequest(token.Error);

            return Ok(token.Value);
        }

        [HttpPost("refresh")]
        public async Task<ActionResult> Refresh([FromBody] TokenRefreshRequest request)
        {
            var result = await _jwtProvider.RefreshTokens(
                request.AccessToken,
                request.RefreshToken
            );

            if (result.IsFailure)
                return Unauthorized(result.Error);

            return Ok(result.Value);
        }
    }
}
