package com.example.shabasher.Model

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val NAME = "name"
    const val PROFILE = "profile"
    const val EVENT = "event"
    const val CREATEEVENT = "createevent"
    const val SHAREEVENT = "shareevent"
    const val PARTICIPANTS = "participants"
    const val PROFILE_WITH_ID = "profile/{userId}"
    const val EDIT_PROFILE = "edit_profile"
    const val DONATION_LIST = "donation_list/{eventId}"

    fun donationList(eventId: String) = "donation_list/$eventId"
    const val DONATION = "donation/{donationId}"

    // ✅ Хелпер для навигации
    fun donation(donationId: String) = "donation/$donationId"
    const val EDITEVENT = "edit_event/{eventId}"

    const val SUGGESTIONS = "suggestions/{eventId}"

    fun suggestions(eventId: String) = "suggestions/$eventId"

    const val CREATE_FUNDRAISE = "create_fundraise/{eventId}"

    fun createFundraise(eventId: String) = "create_fundraise/$eventId"
}
