using System.Text.Json.Serialization;

namespace Shabasher.Core.Models
{
    [JsonConverter(typeof(JsonStringEnumConverter))]
    public enum ShabashRole
    {
        Member = 0,
        CoAdmin = 1,
        Admin = 2
    }
}