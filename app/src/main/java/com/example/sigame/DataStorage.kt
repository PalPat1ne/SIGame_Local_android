package com.example.sigame

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class DataStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var playerName: String?
        get() = prefs.getString("player_name", null)
        set(value) {
            prefs.edit().putString("player_name", value).apply()
        }

    var birthDate: String?
        get() = prefs.getString("birth_date", null)
        set(value) {
            prefs.edit().putString("birth_date", value).apply()
        }

    var server: String?
        get() = prefs.getString("server", null)
        set(value) {
            prefs.edit().putString("server", value).apply()
        }

    var port: Int
        get() = prefs.getInt("port", 0)
        set(value) {
            prefs.edit().putInt("port", value).apply()
        }

    var uuid: String?
        get() = prefs.getString("uuid", null)
        set(value) {
            prefs.edit().putString("uuid", value).apply()
        }

    fun generateAndStoreUUIDIfNeeded() {
        if (uuid.isNullOrEmpty()) {
            val newUUID = UUID.randomUUID().toString()
            uuid = newUUID
        }
    }
}
