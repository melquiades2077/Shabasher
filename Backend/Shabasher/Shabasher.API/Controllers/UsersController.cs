using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using System.Security.Claims;

namespace Shabasher.API.Controllers
{
    [Route("api/[controller]")]
    [Authorize]
    [ApiController]
    public class UsersController : ControllerBase
    {
        private readonly IUsersManageService _usersManageService;

        public UsersController(IUsersManageService usersManageService)
        {
            _usersManageService = usersManageService;
        }

        private string GetUserId()
        {
            return User.FindFirstValue("userId") ?? string.Empty;
        }

        [HttpGet("by-id")]
        public async Task<ActionResult<UserResponse>> GetUserById([FromQuery] string id)
        {
            var user = await _usersManageService.GetUserByIdAsync(id);

            if (user.IsFailure)
                return NotFound(user.Error);

            return Ok(user.Value);
        }

        [HttpGet("by-email")]
        public async Task<ActionResult<UserResponse>> GetUserByEmail([FromQuery] string email)
        {
            var user = await _usersManageService.GetUserByEmailAsync(email);

            if (user.IsFailure)
                return NotFound(user.Error);

            return Ok(user.Value);
        }

        //[HttpPatch("username")]
        //public async Task<ActionResult> UpdateUserName([FromBody] UpdateUserNameRequest updateUserNameRequest)
        //{
        //    var updateResult = await _usersManageService.UpdateUserNameAsync(updateUserNameRequest.Id, updateUserNameRequest.Name);

        //    if (updateResult.IsFailure)
        //        return BadRequest(updateResult.Error);

        //    return Ok(updateUserNameRequest.Name);
        //}

        [HttpPatch("profile")]
        public async Task<ActionResult<UserResponse>> UpdateUserProfile([FromBody] UpdateUserProfileRequest request)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _usersManageService.UpdateUserProfileAsync(userId, request.Name, request.AboutMe, request.Telegram);

            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpDelete]
        public async Task<ActionResult> DeleteUser([FromQuery] string userId)
        {
            var deleteResult = await _usersManageService.DeleteUserAsync(userId);

            if (deleteResult.IsFailure)
                return BadRequest(deleteResult.Error);

            return Ok(userId);
        }

        [HttpPatch("status")]
        public async Task<ActionResult> UpdateUserStatus([FromBody] UpdateUserStatusRequest request)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var updateResult = await _usersManageService.UpdatePastorStatusAsync(userId, request.ShabashId, request.Status);

            if (updateResult.IsFailure)
                return BadRequest(updateResult.Error);

            return Ok(updateResult.Value);
        }
    }
}
