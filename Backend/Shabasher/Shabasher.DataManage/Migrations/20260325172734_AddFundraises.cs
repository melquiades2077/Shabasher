using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Shabasher.DataManage.Migrations
{
    /// <inheritdoc />
    public partial class AddFundraises : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "Fundraises",
                columns: table => new
                {
                    Id = table.Column<string>(type: "text", nullable: false),
                    Name = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    EventId = table.Column<string>(type: "text", nullable: false),
                    CreatorId = table.Column<string>(type: "text", nullable: false),
                    CreatorPhone = table.Column<string>(type: "text", nullable: false),
                    CreatorName = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    Description = table.Column<string>(type: "character varying(600)", maxLength: 600, nullable: true),
                    TargetAmount = table.Column<decimal>(type: "numeric", nullable: true),
                    CurrentAmount = table.Column<decimal>(type: "numeric", nullable: false, defaultValue: 0m),
                    FundStatus = table.Column<int>(type: "integer", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false, defaultValueSql: "NOW()")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Fundraises", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "FundraiseParticipants",
                columns: table => new
                {
                    Id = table.Column<string>(type: "text", nullable: false),
                    FundraiseId = table.Column<string>(type: "text", nullable: false),
                    UserId = table.Column<string>(type: "text", nullable: false),
                    Amount = table.Column<decimal>(type: "numeric", nullable: false),
                    Status = table.Column<int>(type: "integer", nullable: false),
                    PaidAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false, defaultValueSql: "NOW()"),
                    CheckedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_FundraiseParticipants", x => x.Id);
                    table.ForeignKey(
                        name: "FK_FundraiseParticipants_Fundraises_FundraiseId",
                        column: x => x.FundraiseId,
                        principalTable: "Fundraises",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_FundraiseParticipants_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateIndex(
                name: "IX_FundraiseParticipants_FundraiseId_UserId",
                table: "FundraiseParticipants",
                columns: new[] { "FundraiseId", "UserId" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_FundraiseParticipants_UserId",
                table: "FundraiseParticipants",
                column: "UserId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "FundraiseParticipants");

            migrationBuilder.DropTable(
                name: "Fundraises");
        }
    }
}
