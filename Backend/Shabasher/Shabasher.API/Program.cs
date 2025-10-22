using Microsoft.EntityFrameworkCore;
using Shabasher.BusinessLogic.Services;
using Shabasher.Core.Interfaces;
using Shabasher.DataManage;

var builder = WebApplication.CreateBuilder(args);


builder.Services.AddControllers();
builder.Services.AddScoped<IUsersManageService, UsersManageService>();
builder.Services.AddScoped<IPasswordHasher, Shabasher.Core.PasswordHasher>();
builder.Services.AddDbContext<ShabasherDbContext>(options =>
    {
        options.UseNpgsql(builder.Configuration.GetConnectionString(nameof(ShabasherDbContext)));
    });

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
}
app.UseRouting();
app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();
