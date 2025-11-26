using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi.Models;
using Shabasher.API.Extensions;
using Shabasher.BusinessLogic.Jwt;
using Shabasher.BusinessLogic.Services;
using Shabasher.Core.Interfaces;
using Shabasher.DataManage;
using DotNetEnv;

Env.Load();

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<JwtOptions>(builder.Configuration.GetSection(nameof(JwtOptions)));
builder.Services.AddApiAuthentication(builder.Configuration);
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo { Title = "Shabasher API", Version = "v1" });

    var securityScheme = new OpenApiSecurityScheme //эти опции выдал товарищ инвестор, без них swagger не отправляет токен
    {
        Name = "Authorization",
        Description = "Enter 'Bearer {token}'",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        Reference = new OpenApiReference
        {
            Type = ReferenceType.SecurityScheme,
            Id = "Bearer"
        }
    };

    options.AddSecurityDefinition("Bearer", securityScheme);
    options.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        { securityScheme, Array.Empty<string>() }
    });
});
builder.Configuration.AddEnvironmentVariables();
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
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();