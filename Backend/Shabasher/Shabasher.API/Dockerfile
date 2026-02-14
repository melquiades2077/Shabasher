FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /app

COPY . .

RUN dotnet restore Shabasher/Shabasher.API/Shabasher.API.csproj
RUN dotnet publish -c Release -o out Shabasher/Shabasher.API/Shabasher.API.csproj

FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app
COPY --from=build /app/out .
EXPOSE 5000
EXPOSE 5001
ENTRYPOINT ["dotnet", "Shabasher.API.dll"]