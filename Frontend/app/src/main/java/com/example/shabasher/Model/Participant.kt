package com.example.shabasher.Model

//Числовые коды для статусов
enum class ParticipationStatus(val code: Int) {
    INVITED(0),
    GOING(1),
    NOT_GOING(2);

    companion object {
        fun fromCode(code: Int): ParticipationStatus {
            return values().firstOrNull { it.code == code } ?: INVITED
        }
    }
}

data class Participant(
    val id: String,
    val name: String,
    val status: ParticipationStatus
)