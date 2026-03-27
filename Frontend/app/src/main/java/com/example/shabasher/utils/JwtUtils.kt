package com.example.shabasher.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

object JwtUtils {

    fun decodeClaim(token: String, claimName: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            val json = String(payload)
            val jsonObject = JSONObject(json)
            Log.d("JwtDebug", "Token payload: $json")

            jsonObject.optString(claimName).takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    fun decodeUserId(token: String): String? = decodeClaim(token, "userId")
    fun decodeUserName(token: String): String? = decodeClaim(token, "name")
        ?: decodeClaim(token, "userName" )
        ?: decodeClaim(token, "username")

}