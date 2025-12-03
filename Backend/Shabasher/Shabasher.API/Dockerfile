FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY . .

RUN dotnet restore Shabasher/Shabasher.API/Shabasher.API.csproj

WORKDIR /src/Shabasher/Shabasher.API
RUN dotnet tool restore

RUN dotnet publish -c Release -o /app/out Shabasher/Shabasher.API.csproj

FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app
COPY --from=build /app/out .
EXPOSE 5000
ENTRYPOINT ["dotnet", "Shabasher.API.dll"]

FROM mcr.microsoft.com/dotnet/sdk:9.0 AS migrations
WORKDIR /app
COPY . .
RUN dotnet restore Shabasher/Shabasher.API/Shabasher.API.csproj
RUN dotnet tool restore
ENTRYPOINT ["dotnet", "tool", "run", "dotnet-ef"]