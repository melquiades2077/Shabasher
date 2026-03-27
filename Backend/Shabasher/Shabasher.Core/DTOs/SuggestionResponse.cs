using Shabasher.Core;
using Shabasher.Core.Models;

namespace Shabasher.Core.DTOs
{
    public record SuggestionResponse(
        string Id,
        string UserId,
        string UserName,
        string Description,
        int Likes,
        int Dislikes,
        DateTime CreatedAt,
        bool Liked,
        bool Disliked,
        ShabashRole UserRole
        );
}
