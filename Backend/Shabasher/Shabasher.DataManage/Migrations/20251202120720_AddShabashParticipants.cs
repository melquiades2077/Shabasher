using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Shabasher.DataManage.Migrations
{
    /// <inheritdoc />
    public partial class AddShabashParticipants : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "ShabashEntityUserEntity");

            migrationBuilder.CreateTable(
                name: "ShabashParticipants",
                columns: table => new
                {
                    ShabashId = table.Column<string>(type: "text", nullable: false),
                    UserId = table.Column<string>(type: "text", nullable: false),
                    Status = table.Column<int>(type: "integer", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ShabashParticipants", x => new { x.ShabashId, x.UserId });
                    table.ForeignKey(
                        name: "FK_ShabashParticipants_Shabashes_ShabashId",
                        column: x => x.ShabashId,
                        principalTable: "Shabashes",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_ShabashParticipants_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_ShabashParticipants_UserId",
                table: "ShabashParticipants",
                column: "UserId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "ShabashParticipants");

            migrationBuilder.CreateTable(
                name: "ShabashEntityUserEntity",
                columns: table => new
                {
                    ParticipantsId = table.Column<string>(type: "text", nullable: false),
                    ShabashesId = table.Column<string>(type: "text", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ShabashEntityUserEntity", x => new { x.ParticipantsId, x.ShabashesId });
                    table.ForeignKey(
                        name: "FK_ShabashEntityUserEntity_Shabashes_ShabashesId",
                        column: x => x.ShabashesId,
                        principalTable: "Shabashes",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_ShabashEntityUserEntity_Users_ParticipantsId",
                        column: x => x.ParticipantsId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_ShabashEntityUserEntity_ShabashesId",
                table: "ShabashEntityUserEntity",
                column: "ShabashesId");
        }
    }
}
