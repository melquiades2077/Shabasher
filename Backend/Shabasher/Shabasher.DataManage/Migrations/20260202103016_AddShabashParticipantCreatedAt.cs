using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Shabasher.DataManage.Migrations
{
    /// <inheritdoc />
    public partial class AddShabashParticipantCreatedAt : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<DateTime>(
                name: "CreatedAt",
                table: "ShabashParticipants",
                type: "timestamp with time zone",
                nullable: false,
                defaultValueSql: "NOW()");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "CreatedAt",
                table: "ShabashParticipants");
        }
    }
}
