FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY . .

RUN dotnet restore Shabasher/Shabasher.API/Shabasher.API.csproj

WORKDIR /src/Shabasher/Shabasher.API
RUN dotnet publish -c Release -o /app/out Shabasher.API.csproj

FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app
COPY --from=build /app/out .
EXPOSE 5000
ENTRYPOINT ["dotnet", "Shabasher.API.dll"]

FROM mcr.microsoft.com/dotnet/sdk:9.0 AS migrations
WORKDIR /src

COPY . .

RUN dotnet tool install --global dotnet-ef --version 9.0.0
ENV PATH="$PATH:/root/.dotnet/tools"

ENTRYPOINT ["dotnet-ef"]