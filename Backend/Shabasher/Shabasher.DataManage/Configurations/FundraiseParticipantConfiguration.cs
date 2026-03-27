using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class FundraiseParticipantConfiguration : IEntityTypeConfiguration<FundraiseParticipantEntity>
    {
        public void Configure(EntityTypeBuilder<FundraiseParticipantEntity> builder)
        {
            builder.HasKey(x => x.Id);

            builder.Property(x => x.Id)
                .IsRequired();

            builder.Property(x => x.FundraiseId)
                .IsRequired();

            builder.Property(x => x.UserId)
                .IsRequired();

            builder.Property(x => x.Amount)
                .IsRequired();

            builder.Property(x => x.Status)
                .IsRequired()
                .HasConversion<int>();

            builder.Property(x => x.PaidAt)
                .IsRequired()
                .HasDefaultValueSql("NOW()");

            builder.Property(x => x.CheckedAt)
                .IsRequired(false);

            builder.HasIndex(x => new { x.FundraiseId, x.UserId })
                .IsUnique();

            builder.HasOne<UserEntity>()
                .WithMany()
                .HasForeignKey(x => x.UserId)
                .OnDelete(DeleteBehavior.Restrict);
        }
    }
}
