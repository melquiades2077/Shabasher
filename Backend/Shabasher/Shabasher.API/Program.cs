using DotNetEnv;
using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi.Models;
using Shabasher.API.Extensions;
using Shabasher.BusinessLogic.Jwt;
using Shabasher.BusinessLogic.Services;
using Shabasher.Core.Interfaces;
using Shabasher.DataManage;
using System.Security.Cryptography.X509Certificates;

Env.Load();

var builder = WebApplication.CreateBuilder(args);

builder.WebHost.UseUrls("http://0.0.0.0:5000");
builder.WebHost.UseUrls("https://0.0.0.0:5001");

builder.WebHost.ConfigureKestrel(options =>
{
    var httpsPort = 5001;
    var certPath = Environment.GetEnvironmentVariable("SSL_CERT_PATH");
    var certPassword = Environment.GetEnvironmentVariable("SSL_CERT_PASSWORD");

    options.ListenAnyIP(httpsPort, listenOptions =>
    {
        listenOptions.Protocols = Microsoft.AspNetCore.Server.Kestrel.Core.HttpProtocols.Http1AndHttp2;
        
        if (!string.IsNullOrEmpty(certPath) && File.Exists(certPath))
        {
            var cert = string.IsNullOrEmpty(certPassword)
                ? new X509Certificate2(certPath)
                : new X509Certificate2(certPath, certPassword);
            listenOptions.UseHttps(cert);
        }
    });
});

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
builder.Services.AddHttpsRedirection(options =>
{
    options.RedirectStatusCode = Microsoft.AspNetCore.Http.StatusCodes.Status307TemporaryRedirect;
    options.HttpsPort = 5001;
});
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

app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseRouting();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();