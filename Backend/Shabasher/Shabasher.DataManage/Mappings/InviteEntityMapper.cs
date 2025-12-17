using Shabasher.Core.Models;
using Shabasher.DataManage.Entities;

namespace Shabasher.DataManage.Mappings
{
    public static class InviteEntityMapper
    {
        public static InviteEntity ToEntity(Invite invite)
        {
            return new InviteEntity
            {
                Id = invite.Id,
                ShabashId = invite.ShabashId,
                InviterUserId = invite.InviterUserId,
                CreatedAt = invite.CreatedAt
            };
        }
    }
}
