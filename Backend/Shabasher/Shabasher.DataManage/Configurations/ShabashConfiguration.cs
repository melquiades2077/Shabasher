using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class ShabashConfiguration: IEntityTypeConfiguration<ShabashEntity>
    {
        public void Configure(EntityTypeBuilder<ShabashEntity> builder)
        {
            builder.HasKey(x => x.Id);

            builder.Property(x => x.Id)
                   .IsRequired();

            builder.Property(x => x.Name)
                   .IsRequired();

            builder.Property(x => x.Description)
                   .HasDefaultValue("");

            builder.Property(x => x.StartDate)
                   .IsRequired();

            builder.Property(x => x.CreatedAt)
                   .IsRequired()
                   .HasDefaultValueSql("NOW()");

            builder.HasMany(p => p.Participants)
                   .WithOne(sp => sp.Shabash)
                   .HasForeignKey(sp => sp.ShabashId);
        }
    }
}
