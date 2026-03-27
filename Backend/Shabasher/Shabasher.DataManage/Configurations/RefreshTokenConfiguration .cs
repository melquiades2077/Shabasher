using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class RefreshTokenConfiguration : IEntityTypeConfiguration<RefreshTokenEntity>
    {
        public void Configure(EntityTypeBuilder<RefreshTokenEntity> builder)
        {
            builder.HasKey(r => r.Id);

            builder.HasIndex(r => r.Token).IsUnique();
            builder.HasIndex(r => r.UserId);

            builder.Property(r => r.Token).HasMaxLength(256).IsRequired();
            builder.Property(r => r.JwtId).HasMaxLength(256).IsRequired();
            builder.Property(r => r.UserId).IsRequired();
        }
    }
}