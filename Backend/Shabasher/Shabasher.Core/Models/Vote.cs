using System.Text.Json.Serialization;

namespace Shabasher.Core.Models
{
    [JsonConverter(typeof(JsonStringEnumConverter))]
    public enum Vote
    {
        Dislike = 0,
        Like = 1
    }
}