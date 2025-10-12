using Microsoft.EntityFrameworkCore;
using Shabasher.DataManage;

var builder = WebApplication.CreateBuilder(args);


builder.Services.AddControllers();
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
app.MapControllers(); // Маршрутизация

app.Run();