using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class FundraiseConfiguration : IEntityTypeConfiguration<FundraiseEntity>
    {
        public void Configure(EntityTypeBuilder<FundraiseEntity> builder)
        {
            builder.HasKey(x => x.Id);

            builder.Property(x => x.Id)
                .IsRequired();

            builder.Property(x => x.Name)
                .IsRequired()
                .HasMaxLength(100);

            builder.Property(x => x.EventId)
                .IsRequired();

            builder.Property(x => x.CreatorId)
                .IsRequired();

            builder.Property(x => x.CreatorPhone)
                .IsRequired();

            builder.Property(x => x.CreatorName)
                .IsRequired()
                .HasMaxLength(100);

            builder.Property(x => x.Description)
                .IsRequired(false)
                .HasMaxLength(600);

            builder.Property(x => x.TargetAmount)
                .IsRequired(false);

            builder.Property(x => x.CurrentAmount)
                .IsRequired()
                .HasDefaultValue(0);

            builder.Property(x => x.FundStatus)
                .IsRequired()
                .HasConversion<int>();

            builder.Property(x => x.CreatedAt)
                .IsRequired()
                .HasDefaultValueSql("NOW()");

            builder.HasMany(f => f.Participants)
                .WithOne()
                .HasForeignKey(p => p.FundraiseId)
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
