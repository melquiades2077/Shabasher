using DotNetEnv;
using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi.Models;
using Shabasher.API.Extensions;
using Shabasher.BusinessLogic.Jwt;
using Shabasher.BusinessLogic.Services;
using Shabasher.Core.Interfaces;
using Shabasher.DataManage;

Env.Load();

var builder = WebApplication.CreateBuilder(args);

builder.WebHost.UseUrls("http://0.0.0.0:5000");

var jwtSecret = Environment.GetEnvironmentVariable("JWT_SECRET");
var jwtHours = Environment.GetEnvironmentVariable("JWT_HOURS");

builder.Services.Configure<JwtOptions>(options =>
{
    options.SecretKey = jwtSecret;
    options.ExpiresHours = Convert.ToInt32(jwtHours);
});
builder.Services.AddApiAuthentication(builder.Configuration);
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo { Title = "Shabasher API", Version = "v1" });

    var securityScheme = new OpenApiSecurityScheme 
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
builder.Services.AddScoped<IShabashesManageService, ShabashesManageService>();
builder.Services.AddScoped<IPasswordHasher, Shabasher.Core.PasswordHasher>();
builder.Services.AddScoped<IJwtProvider, JwtProvider>();
builder.Services.AddDbContext<ShabasherDbContext>(options =>
    {
        options.UseNpgsql(Environment.GetEnvironmentVariable("DB_CONNECTION_STRING"));
    });

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<ShabasherDbContext>();
    db.Database.Migrate();
}

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
app.UseRouting();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.UseStaticFiles();

app.Run();