using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Configurations
{
    public class InviteConfiguration : IEntityTypeConfiguration<InviteEntity>
    {
        public void Configure(EntityTypeBuilder<InviteEntity> builder)
        {
            builder.HasKey(i => i.Id);

            builder.Property(i => i.ShabashId)
                   .IsRequired();

            builder.Property(i => i.InviterUserId)
                   .IsRequired();

            builder.Property(i => i.CreatedAt)
                   .IsRequired();
        }
    }
}