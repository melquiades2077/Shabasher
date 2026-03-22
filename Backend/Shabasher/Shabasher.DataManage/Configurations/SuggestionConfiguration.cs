using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class SuggestionConfiguration : IEntityTypeConfiguration<SuggestionEntity>
    {
        public void Configure(EntityTypeBuilder<SuggestionEntity> builder)
        {
            builder.HasKey(x => x.Id);

            builder.Property(x => x.Id)
                .IsRequired();

            builder.Property(x => x.ShabashId)
                .IsRequired();

            builder.Property(x => x.UserId)
                .IsRequired();

            builder.Property(x => x.Description)
                .IsRequired()
                .HasMaxLength(300)
                .HasDefaultValue("");

            builder.Property(x => x.LikesCount)
                .IsRequired()
                .HasDefaultValue(0);

            builder.Property(x => x.DislikesCount)
                .IsRequired()
                .HasDefaultValue(0);

            builder.Property(x => x.CreatedAt)
                .IsRequired()
                .HasDefaultValueSql("NOW()");

            builder.HasOne(x => x.Shabash)
                .WithMany()
                .HasForeignKey(x => x.ShabashId)
                .OnDelete(DeleteBehavior.Cascade);

            builder.HasOne(x => x.User)
                .WithMany()
                .HasForeignKey(x => x.UserId)
                .OnDelete(DeleteBehavior.Restrict);

            builder.HasMany(x => x.Votes)
                .WithOne(v => v.Suggestion)
                .HasForeignKey(v => v.SuggestionId)
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
