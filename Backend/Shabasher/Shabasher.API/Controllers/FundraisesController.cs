using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using System.Security.Claims;

namespace Shabasher.API.Controllers
{
    [Route("api/fundraises")]
    [Authorize]
    [ApiController]
    public class FundraisesController : ControllerBase
    {
        private readonly IFundraisesManageService _fundraisesManageService;

        public FundraisesController(IFundraisesManageService fundraisesManageService)
        {
            _fundraisesManageService = fundraisesManageService;
        }

        private string GetUserId()
        {
            return User.FindFirstValue("userId") ?? string.Empty;
        }

        [HttpGet("{fundraiseId}")]
        public async Task<ActionResult<FundraiseDetailsResponse>> GetFundraise(string fundraiseId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _fundraisesManageService.GetFundraiseAsync(fundraiseId, userId);
            if (result.IsFailure)
                return BadRequest(result.Error);

            return Ok(result.Value);
        }

        [HttpPost("{fundraiseId}/close")]
        public async Task<ActionResult> CloseFundraise(string fundraiseId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var response = await _fundraisesManageService.CloseFundraiseAsync(fundraiseId, userId);
            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok();
        }

        [HttpPost("{fundraiseId}/mark-paid")]
        public async Task<ActionResult> MarkPaid(string fundraiseId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var response = await _fundraisesManageService.MarkPaidAsync(fundraiseId, userId);
            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok();
        }

        [HttpPost("{fundraiseId}/participants/{userId}/confirm")]
        public async Task<ActionResult> ConfirmPayment(string fundraiseId, string userId, [FromBody] decimal? amount)
        {
            var adminId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var response = await _fundraisesManageService.ConfirmPaymentAsync(fundraiseId, userId, adminId, amount);
            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok();
        }

        [HttpPost("{fundraiseId}/participants/{userId}/revert")]
        public async Task<ActionResult> RevertPayment(string fundraiseId, string userId)
        {
            var adminId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var response = await _fundraisesManageService.RevertPaymentAsync(fundraiseId, userId, adminId);
            if (response.IsFailure)
                return BadRequest(response.Error);

            return Ok();
        }
    }
}
