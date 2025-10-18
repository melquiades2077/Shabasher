using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class ShabashConfiguration: IEntityTypeConfiguration<ShabashEntity>
    {
        public void Configure(EntityTypeBuilder<ShabashEntity> builder)
        {
            builder.HasKey(e => e.Id);
            builder.HasMany(e => e.Participants).WithMany(p => p.Shabashes);
        }
    }
}
