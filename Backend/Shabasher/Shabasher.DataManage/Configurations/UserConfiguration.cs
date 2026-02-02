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

            builder.Property(x => x.Id)
               .IsRequired();

            builder.Property(x => x.Name)
                   .IsRequired();

            builder.Property(x => x.Email)
                   .IsRequired();

            builder.HasIndex(x => x.Email)
                   .IsUnique();

            builder.Property(x => x.PasswordHash)
                   .IsRequired();

            builder.Property(x => x.CreatedAt)
                   .IsRequired()
                   .HasDefaultValueSql("NOW()");

            builder.Property(x => x.AboutMe)
                   .IsRequired(false);

            builder.Property(x => x.Telegram)
                   .IsRequired(false);

            builder.HasMany(u => u.Participations)
                   .WithOne(sp => sp.User)
                   .HasForeignKey(sp => sp.UserId);
        }
    }
}
