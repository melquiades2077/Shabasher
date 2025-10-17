using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class UserConfiguration : IEntityTypeConfiguration<UserEntity>
    {
        public void Configure(EntityTypeBuilder<UserEntity> builder)
        {
            builder.HasKey(x => x.Id);
            builder.HasMany(x => x.Shabashes).WithMany(y => y.Participants);
        }
    }
}
