using DotNetEnv;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using System.Security.Claims;
using Microsoft.AspNetCore.StaticFiles;
using Microsoft.Extensions.FileProviders;

namespace Shabasher.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class InvitesController : ControllerBase
    {
        private readonly IShabashesManageService _shabashesManageService;
        private readonly IWebHostEnvironment _env;
        private readonly FileExtensionContentTypeProvider _contentTypeProvider = new FileExtensionContentTypeProvider();

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

            var path = Path.Combine(_env.WebRootPath ?? Path.Combine(_env.ContentRootPath, "wwwroot"), "invite.html");

            if (!System.IO.File.Exists(path))
                return NotFound($"Invite page not found: {path}");

            return PhysicalFile(path, "text/html");
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

        [HttpGet("download/{fileName?}")]
        public async Task<ActionResult> DownloadApk(string fileName = null)
        {
            var filesDirectory = _env.WebRootPath ?? Path.Combine(_env.ContentRootPath, "wwwroot");

            var apkFiles = Directory.GetFiles(filesDirectory, "*.apk");

            if (apkFiles.Length == 0)
                return NotFound("No APK files found");

            var latestFile = apkFiles
                .OrderByDescending(f => new FileInfo(f).LastWriteTime)
                .FirstOrDefault();

            string apkPath = latestFile;

            if (!System.IO.File.Exists(apkPath))
                return NotFound();

            var fileInfo = new FileInfo(apkPath);

            if (!_contentTypeProvider.TryGetContentType(apkPath, out var contentType))
                contentType = "application/vnd.android.package-archive";

            return PhysicalFile(apkPath, contentType, fileInfo.Name);
        }
    }
}
