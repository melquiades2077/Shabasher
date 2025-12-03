FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /app

COPY . .
RUN dotnet tool install --global dotnet-ef
ENV PATH="${PATH}:/root/.dotnet/tools"
RUN dotnet restore Shabasher/Shabasher.API/Shabasher.API.csproj
RUN dotnet publish -c Release -o out Shabasher/Shabasher.API/Shabasher.API.csproj

FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app
COPY --from=build /app/out .
COPY --from=build /root/.dotnet /root/.dotnet
ENV PATH="${PATH}:/root/.dotnet/tools"
EXPOSE 5000
ENTRYPOINT ["dotnet", "Shabasher.API.dll"]