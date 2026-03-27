using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Shabasher.Core.DTOs;
using Shabasher.Core.Interfaces;
using System.Security.Claims;

namespace Shabasher.API.Controllers
{
    [Route("api/suggestions")]
    [Authorize]
    [ApiController]
    public class SuggestionsController : ControllerBase
    {
        private readonly ISuggestionsManageService _suggestions;

        public SuggestionsController(ISuggestionsManageService suggestions)
        {
            _suggestions = suggestions;
        }

        private string GetUserId() => User.FindFirstValue("userId") ?? string.Empty;

        [HttpPost("{suggestionId}/vote")]
        public async Task<ActionResult<SuggestionVoteResultResponse>> Vote(string suggestionId, [FromBody] string voteSuggestionAction)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _suggestions.VoteAsync(suggestionId, userId, voteSuggestionAction);
            if (result.IsFailure)
            {
                if (result.Error == "Предложение не найдено")
                    return NotFound(result.Error);
                return BadRequest(result.Error);
            }

            return Ok(result.Value);
        }

        [HttpDelete("{suggestionId}")]
        public async Task<IActionResult> Delete(string suggestionId)
        {
            var userId = GetUserId();
            if (string.IsNullOrEmpty(userId))
                return Unauthorized("Не удалось определить пользователя");

            var result = await _suggestions.DeleteSuggestionAsync(suggestionId, userId);
            if (result.IsFailure)
            {
                if (result.Error == "Предложение не найдено")
                    return NotFound(result.Error);
                return BadRequest(result.Error);
            }

            return NoContent();
        }
    }
}
