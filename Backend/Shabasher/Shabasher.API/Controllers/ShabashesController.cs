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
        private readonly ISuggestionsManageService _suggestionsManageService;
        private readonly IFundraisesManageService _fundraisesManageService;
        private readonly IFilesManageService _filesManageService;

        public ShabashesController(IShabashesManageService shabashesManageService, ISuggestionsManageService suggestionsManageService, IFundraisesManageService fundraisesManageService, IFilesManageService filesManageService)
        {
            _shabashesManageService = shabashesManageService;
            _suggestionsManageService = suggestionsManageService;
            _fundraisesManageService = fundraisesManageService;
            _filesManageService = filesManageService;
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

        [HttpDelete("{shabashId}/avatar")]
        public async Task<ActionResult<ShabashResponse>> DeleteShabashAvatar([FromRoute] string shabashId)
        {
            var cancellationToken = HttpContext.RequestAborted;

            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _shabashesManageService.RemoveShabashAvatarAsync(shabashId, userId);
            if (result.IsFailure)
            {
                if (result.Error == "У пользователя недостаточно прав")
                    return StatusCode(StatusCodes.Status403Forbidden);
                if (result.Error == "Шабаш не найден")
                    return NotFound(result.Error);
                return BadRequest(result.Error);
            }

            if (!string.IsNullOrWhiteSpace(result.Value.OldObjectKey))
                await _filesManageService.DeleteFileAsync(result.Value.OldObjectKey!, cancellationToken);

            return Ok(result.Value.Shabash);
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
            var response = await _shabashesManageService.KickFromShabashAsync(request.UserId, request.AdminId, request.ShabashId);

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

        [HttpGet("{shabashId}/suggestions")]
        public async Task<ActionResult<SuggestionsListResponse>> GetSuggestions(string shabashId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _suggestionsManageService.GetSuggestionsAsync(shabashId, userId);
            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpPost("{shabashId}/suggestions")]
        public async Task<ActionResult<SuggestionResponse>> CreateSuggestion(string shabashId, [FromBody] string createSuggestionText)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _suggestionsManageService.CreateSuggestionAsync(shabashId, userId, createSuggestionText);
            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpGet("{shabashId}/fundraises")]
        public async Task<ActionResult<FundraisesListResponse>> GetAllFundraises(string shabashId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _fundraisesManageService.GetAllFundraisesAsync(shabashId, userId);
            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpPost("{shabashId}/fundraises")]
        public async Task<ActionResult<FundraiseDetailsResponse>> CreateFundraise(string shabashId, [FromBody] CreateFundraiseRequest request)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _fundraisesManageService.CreateFundraiseAsync(shabashId, userId, request);
            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpPost("{shabashId}/avatar")]
        [Consumes("multipart/form-data")]
        public async Task<ActionResult<ShabashResponse>> UploadShabashAvatar([FromRoute] string shabashId, IFormFile file)
        {
            var cancellationToken = HttpContext.RequestAborted;

            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            if (file == null || file.Length == 0)
                return BadRequest("Файл не передан");
            if (file.Length > 5 * 1024 * 1024)
                return BadRequest("Максимальный размер файла 5MB");

            var allowedTypes = new[] { "image/jpeg", "image/png", "image/webp" };
            if (!allowedTypes.Contains(file.ContentType))
                return BadRequest("Поддерживаются только JPEG/PNG/WEBP");

            await using var stream = file.OpenReadStream();
            var uploaded = await _filesManageService.UploadImageAsync(
                stream,
                file.FileName,
                file.ContentType,
                $"shabashes/{shabashId}/avatar",
                cancellationToken);
            if (uploaded.IsFailure)
                return BadRequest(uploaded.Error);

            var update = await _shabashesManageService.UpdateShabashAvatarAsync(shabashId, userId, uploaded.Value.Url, uploaded.Value.ObjectKey);
            if (update.IsFailure)
            {
                if (update.Error == "У пользователя недостаточно прав")
                    return StatusCode(StatusCodes.Status403Forbidden);
                if (update.Error == "Шабаш не найден")
                    return NotFound(update.Error);
                return BadRequest(update.Error);
            }

            return Ok(update.Value);
        }
    }
}
