using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class ShabashParticipantConfiguration : IEntityTypeConfiguration<ShabashParticipantEntity>
    {
        public void Configure(EntityTypeBuilder<ShabashParticipantEntity> builder)
        {
            builder.HasKey(sp => new { sp.ShabashId, sp.UserId });

            builder.Property(sp => sp.Status)
                   .IsRequired();

            builder.Property(sp => sp.Role)
                   .IsRequired();

            builder.Property(sp => sp.CreatedAt)
                   .IsRequired()
                   .HasDefaultValueSql("NOW()");
        }
    }
}


