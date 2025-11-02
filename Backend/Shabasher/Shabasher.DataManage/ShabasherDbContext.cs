using Microsoft.EntityFrameworkCore;
using Shabasher.DataManage.Configurations;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage
{
    public class ShabasherDbContext : DbContext
    {
        public ShabasherDbContext(DbContextOptions<ShabasherDbContext> options) : base(options)
        {
            
        }

        public DbSet<UserEntity> Users { get; set; }
        public DbSet<ShabashEntity> Shabashes { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.ApplyConfiguration(new UserConfiguration());
            modelBuilder.ApplyConfiguration(new ShabashConfiguration());

            base.OnModelCreating(modelBuilder);
        }
    }
}
