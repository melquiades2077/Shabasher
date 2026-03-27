using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class SuggestionVoteConfiguration : IEntityTypeConfiguration<SuggestionVoteEntity>
    {
        public void Configure(EntityTypeBuilder<SuggestionVoteEntity> builder)
        {
            builder.HasKey(x => x.Id);

            builder.Property(x => x.Id)
                .IsRequired();

            builder.Property(x => x.SuggestionId)
                .IsRequired();

            builder.Property(x => x.UserId)
                .IsRequired();

            builder.Property(x => x.Vote)
                .IsRequired()
                .HasConversion<int>();

            builder.Property(x => x.CreatedAt)
                .IsRequired()
                .HasDefaultValueSql("NOW()");

            builder.HasOne(x => x.User)
                .WithMany()
                .HasForeignKey(x => x.UserId)
                .OnDelete(DeleteBehavior.Restrict);

            // Один голос от пользователя на одно предложение (изменение голоса = update сущности)
            builder.HasIndex(x => new { x.SuggestionId, x.UserId })
                .IsUnique();
        }
    }
}
