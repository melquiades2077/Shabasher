using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

//программа падает с ошибкой потому что swagger не может сам составить схему для файла, китайский партнер сказать сделать так:
public class FileUploadOperationFilter : IOperationFilter
{
    public void Apply(OpenApiOperation operation, OperationFilterContext context)
    {
        var fileParams = context.MethodInfo.GetParameters()
            .Where(p => p.ParameterType == typeof(IFormFile))
            .ToList();

        if (fileParams.Count == 0) return;

        operation.Parameters?.Clear();

        operation.RequestBody = new OpenApiRequestBody
        {
            Content = new Dictionary<string, OpenApiMediaType>
            {
                ["multipart/form-data"] = new OpenApiMediaType
                {
                    Schema = new OpenApiSchema
                    {
                        Type = "object",
                        Properties = fileParams.ToDictionary(
                            p => p.Name!,
                            p => new OpenApiSchema
                            {
                                Type = "string",
                                Format = "binary"
                            }),
                        Required = new HashSet<string>(fileParams.Select(p => p.Name!))
                    }
                }
            }
        };
    }
}