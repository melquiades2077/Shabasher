using Microsoft.EntityFrameworkCore;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage
{
    public class ShabasherDbContext : DbContext
    {
        public ShabasherDbContext(DbContextOptions<ShabasherDbContext> options) : base(options)
        {
            
        }

        public DbSet<UserEntity> Users { get; set; }
    }
}
