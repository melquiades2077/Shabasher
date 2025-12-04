using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Mappings
{
    public static class ShabashEntityMapper
    {
        public static ShabashEntity ToEntity(Shabash shabash)
        {
            return new ShabashEntity
            {
                Id = shabash.Id,
                Name = shabash.Name,
                Description = shabash.Description,
                Address = shabash.Address,
                StartDate = shabash.StartDate,
                CreatedAt = shabash.CreatedAt
            };
        }
    }
}
