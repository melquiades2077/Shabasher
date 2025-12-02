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
        public async Task<ActionResult<string>> CreateShabash([FromBody] CreateShabashRequest request)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _shabashesManageService.CreateShabashAsync(request, userId);

            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpGet("by-id")]
        public async Task<ActionResult<ShabashResponse>> GetShabashById([FromQuery] string id)
        {
            var result = await _shabashesManageService.GetShabashByIdAsync(id);

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
    }
}
