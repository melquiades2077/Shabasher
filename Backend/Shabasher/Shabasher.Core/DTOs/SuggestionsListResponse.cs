namespace Shabasher.Core.DTOs
{
    public record SuggestionsListResponse(IReadOnlyList<SuggestionResponse> Suggestions);
}
