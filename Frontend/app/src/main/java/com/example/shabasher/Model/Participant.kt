package com.example.shabasher.Model

//Числовые коды для статусов
enum class ParticipationStatus(val code: Int, val priority: Int) {
    GOING(1, 0),
    INVITED(0, 1),
    NOT_GOING(2, 2);

    companion object {
        fun fromCode(code: Int): ParticipationStatus {
            return values().firstOrNull { it.code == code } ?: INVITED
        }
    }
}


enum class UserRole(val backendValue: String, val priority: Int) {
    ADMIN("Admin", 0),
    MODERATOR("CoAdmin", 1),
    MEMBER("Member", 2)
}



data class Participant(
    val id: String,
    val name: String,
    val status: ParticipationStatus,
    val role: UserRole = UserRole.MODERATOR // ← пока по умолчанию MEMBER
)