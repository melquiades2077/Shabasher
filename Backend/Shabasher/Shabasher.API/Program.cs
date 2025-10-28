using Microsoft.EntityFrameworkCore;
using Shabasher.BusinessLogic.Jwt;
using Shabasher.BusinessLogic.Services;
using Shabasher.Core.Interfaces;
using Shabasher.DataManage;

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<JwtOptions>(builder.Configuration.GetSection(nameof(JwtOptions)));
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddControllers();
builder.Services.AddScoped<IUsersManageService, UsersManageService>();
builder.Services.AddScoped<IPasswordHasher, Shabasher.Core.PasswordHasher>();
builder.Services.AddScoped<IJwtProvider, JwtProvider>();
builder.Services.AddDbContext<ShabasherDbContext>(options =>
    {
        options.UseNpgsql(builder.Configuration.GetConnectionString(nameof(ShabasherDbContext)));
    });

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
app.UseRouting();
app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();
