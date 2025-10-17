using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class ShabashConfiguration: IEntityTypeConfiguration<ShabashEntity>
    {
        public void Configure(EntityTypeBuilder<ShabashEntity> builder)
        {

        }
    }
}
