using DotNetEnv;
using Microsoft.AspNetCore.HttpOverrides;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using Shabasher.API.Extensions;
using Shabasher.BusinessLogic.Jwt;
using Shabasher.BusinessLogic.Services;
using Shabasher.Core.Interfaces;
using Shabasher.DataManage;
using System.Text;
using System.Text.Json.Serialization;

Env.Load();

var builder = WebApplication.CreateBuilder(args);

builder.WebHost.UseUrls("http://0.0.0.0:5000");

var jwtSecret = Environment.GetEnvironmentVariable("JWT_SECRET");
var jwtHours = Environment.GetEnvironmentVariable("JWT_HOURS");

builder.Services.Configure<JwtOptions>(options =>
{
    options.SecretKey = jwtSecret;
    options.AccessTokenExpiresMinutes = Convert.ToInt32(Environment.GetEnvironmentVariable("JWT_ACCESS_MINUTES"));
    options.RefreshTokenExpirationDays = Convert.ToInt32(Environment.GetEnvironmentVariable("JWT_REFRESH_DAYS"));
});
var tokenValidationParameters = new TokenValidationParameters
{
    ValidateIssuerSigningKey = true,
    IssuerSigningKey = new SymmetricSecurityKey(
        Encoding.UTF8.GetBytes(jwtSecret)),
    ValidateIssuer = false,
    ValidateAudience = false,
    ValidateLifetime = true,
    ClockSkew = TimeSpan.FromSeconds(30)
};
builder.Services.AddSingleton(tokenValidationParameters);
builder.Services.AddApiAuthentication(builder.Configuration);
builder.Services.AddEndpointsApiExplorer();
builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.WriteIndented = true;
    options.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
});
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

builder.Services.Configure<ForwardedHeadersOptions>(options =>
{
    options.ForwardedHeaders = ForwardedHeaders.XForwardedFor | ForwardedHeaders.XForwardedProto;
    options.KnownNetworks.Clear();
    options.KnownProxies.Clear();
});

if (Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") != "Development")
{
    builder.Services.AddHttpsRedirection(options =>
    {
        options.RedirectStatusCode = Microsoft.AspNetCore.Http.StatusCodes.Status307TemporaryRedirect;
        options.HttpsPort = 443;
    });
}
builder.Services.AddScoped<IUsersManageService, UsersManageService>();
builder.Services.AddScoped<IShabashesManageService, ShabashesManageService>();
builder.Services.AddScoped<ISuggestionsManageService, SuggestionsManageService>();
builder.Services.AddScoped<IFundraisesManageService, FundraisesManageService>();
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

app.UseForwardedHeaders();
app.UseHttpsRedirection();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseStaticFiles();
app.UseRouting();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();