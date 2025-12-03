using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;
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

        public AuthController(IUsersManageService usersManageService)
        {
            _usersManageService = usersManageService;
        }

        [HttpPost("register")]
        public async Task<ActionResult<UserResponse>> RegisterUser([FromBody] RegisterUserRequest registerUserRequest)
        {
            var user = await _usersManageService.RegisterUserAsync(
                registerUserRequest.Name,
                registerUserRequest.Email,
                registerUserRequest.Password
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

        [HttpPost("set-name")]
        [Authorize]
        public async Task<IActionResult> SetUserName([FromBody] SetNameRequest request)
        {
            // Искать по "userId" вместо ClaimTypes.NameIdentifier
            var userId = User.FindFirst("userId")?.Value;

            if (string.IsNullOrEmpty(userId))
                return Unauthorized();

            var result = await _usersManageService.UpdateUserNameAsync(userId, request.Name);

            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(new { message = "Имя установлено", name = result.Value });
        }
    }
}
