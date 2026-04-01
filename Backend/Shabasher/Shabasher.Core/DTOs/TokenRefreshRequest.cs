using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Shabasher.Core.DTOs
{
    public record TokenRefreshRequest(
        string AccessToken,
        string RefreshToken
    );
}