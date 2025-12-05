using DotNetEnv;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using System.Security.Claims;

namespace Shabasher.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class InvitesController : ControllerBase
    {
        private readonly IShabashesManageService _shabashesManageService;
        private readonly IWebHostEnvironment _env;

        private string GetUserId()
        {
            return User.FindFirstValue("userId") ?? string.Empty;
        }

        public InvitesController(IShabashesManageService shabashesManageService, IWebHostEnvironment env)
        {
            _shabashesManageService = shabashesManageService;
            _env = env;
        }

        [HttpPost("create")]
        [Authorize]
        public async Task<ActionResult> CreateInvite([FromQuery] string shabashId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var inviteUrl = await _shabashesManageService.CreateInviteAsync(shabashId, userId);

            if (inviteUrl.IsFailure)
                return BadRequest(inviteUrl.Error);

            return Ok(inviteUrl.Value);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult> GetInvitePage(string id)
        {
            var invite = await _shabashesManageService.GetInviteAsync(id);
            if (invite.IsFailure)
                return NotFound(invite.Error);

            var shabashEntity = await _shabashesManageService.GetShabashByIdAsync(invite.Value.ShabashId);
            if (shabashEntity.IsFailure || invite.Value.ExpiresAt <= DateTime.UtcNow)
                return BadRequest("Приглашение недействительно");

            var filePath = Path.Combine(_env.ContentRootPath, "Pages", "invite.html");
            return PhysicalFile(filePath, "text/html");
        }

        [HttpGet("{id}/details")]
        public async Task<ActionResult> GetShabashInfoViaLink(string id)
        {
            var invite = await _shabashesManageService.GetInviteAsync(id);

            if (invite.IsFailure)
                return NotFound(invite.Error);

            var shabashEntity = await _shabashesManageService.GetShabashByIdAsync(invite.Value.ShabashId);

            if (shabashEntity.IsFailure || invite.Value.ExpiresAt <= DateTime.UtcNow)
                return BadRequest("Приглашение недействительно");

            return Ok(new
            {
                shabashEntity.Value.Name,
                shabashEntity.Value.Address,
                shabashEntity.Value.StartDate,
                shabashEntity.Value.StartTime,
                shabashEntity.Value.Id
            });
        }

        [HttpPatch("join")]
        [Authorize]
        public async Task<ActionResult> JoinShabash([FromQuery] string shabashId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var response = await _shabashesManageService.JoinShabashAsync(userId, shabashId);

            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok(response.Value);
        }

        [HttpGet("download")]
        public async Task<ActionResult> DownloadApk()
        {
            var filePath = Path.Combine(_env.ContentRootPath, "Files", "shabasher-v0.1.apk");

            if (!System.IO.File.Exists(filePath))
                return NotFound();

            var fileBytes = System.IO.File.ReadAllBytes(filePath);
            return File(fileBytes, "application/vnd.android.package-archive", "shabasher-v0.1.apk");
        }
    }
}
