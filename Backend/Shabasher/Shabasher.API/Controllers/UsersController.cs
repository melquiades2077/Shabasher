using Microsoft.AspNetCore.Mvc;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;

namespace Shabasher.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UsersController : ControllerBase
    {
        private readonly IUsersManageService _usersManageService;

        public UsersController(IUsersManageService usersManageService)
        {
            _usersManageService = usersManageService;
        }

        [HttpGet]
        public async Task<ActionResult<UserResponse>> GetUserById([FromQuery] string id)
        {
            var user = await _usersManageService.GetUserByIdAsync(id);

            if (user.IsFailure)
                return NotFound(user.Error);

            return Ok(user);
        }

        [HttpPost]
        public async Task<ActionResult<UserResponse>> CreateUser([FromBody] CreateUserRequest userRequest)
        {
            var user = await _usersManageService.CreateUserAsync(
                userRequest.Name, 
                userRequest.Email, 
                userRequest.Password
                );

            if (user.IsFailure)
                return BadRequest(user.Error);

            return Ok(user);
        }
    }
}
