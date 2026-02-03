using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Shabasher.BusinessLogic.Services;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using Shabasher.Core.Models;
using System.Security.Claims;

namespace Shabasher.API.Controllers
{
    [Route("api/[controller]")]
    [Authorize]
    [ApiController]
    public class ShabashesController : ControllerBase
    {
        private readonly IShabashesManageService _shabashesManageService;

        public ShabashesController(IShabashesManageService shabashesManageService)
        {
            _shabashesManageService = shabashesManageService;
        }

        private string GetUserId()
        {
            return User.FindFirstValue("userId") ?? string.Empty;
        }

        [HttpPost]
        public async Task<ActionResult> CreateShabash([FromBody] CreateShabashRequest request)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var localStart = request.StartDate.ToDateTime(request.StartTime);
            var utcStart = DateTime.SpecifyKind(localStart, DateTimeKind.Utc);

            var result = await _shabashesManageService.CreateShabashAsync(
                request.Name,
                request.Description,
                request.Address,
                utcStart,
                userId,
                []);

            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpGet("by-id")]
        public async Task<ActionResult> GetShabashById([FromQuery] string id)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _shabashesManageService.GetShabashByIdAsync(id, userId);

            if (result.IsFailure)
                return NotFound(result.Error);

            return Ok(result.Value);
        }

        [HttpPatch]
        public async Task<ActionResult<ShabashResponse>> UpdateShabash([FromBody] UpdateShabashRequest request)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _shabashesManageService.UpdateShabashAsync(request.Id, request, userId);

            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpDelete]
        public async Task<ActionResult> DeleteShabash([FromQuery] string shabashId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _shabashesManageService.DeleteShabashAsync(shabashId, userId);

            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(shabashId);
        }

        [HttpPatch("leave")]
        public async Task<ActionResult> LeaveShabash([FromQuery]string shabashId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var response = await _shabashesManageService.LeaveShabashAsync(userId, shabashId);

            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok();
        }

        [HttpPatch("kick")]
        public async Task<ActionResult> KickFromShabash([FromBody]KickParticipantRequest request)
        {
            var response = await _shabashesManageService.LeaveShabashAsync(request.UserId, request.ShabashId);

            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok();
        }

        [HttpPatch("roles")]
        public async Task<ActionResult> UpdateParticipantRole([FromBody]UpdateRoleRequest request)
        {
            var adminId = GetUserId();
            if (string.IsNullOrEmpty(adminId))
                return Unauthorized("Не удалось определить пользователя");

            var response = await _shabashesManageService.UpdateParticipantRoleAsync(request.ShabashId, request.UserId, adminId, request.Role);
        
            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok();
        }
    }
}
