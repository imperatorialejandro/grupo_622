package com.tpsoa.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {
    private const val APP_SETTINGS = "com.tpsoa.voicerecorder"

    private const val USER_LOGGER = "user_logged"
    private const val TOKEN = "token"
    private const val RECORDEDVOICENOTE = "recordedVoiceNote"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }

    fun getToken(context: Context): String? {
        return sharedPreferences.getString(TOKEN, "")
    }

    fun setToken(newValue: String) {
        val editor =
            sharedPreferences.edit()
        editor.putString(TOKEN, newValue)
        editor.commit()
    }

    fun getUserLogged(): String? {
        return sharedPreferences
            .getString(USER_LOGGER, "")
    }

    fun setUserLogged(newValue: String) {
        val editor =sharedPreferences.edit()
        editor.putString(USER_LOGGER, newValue)
        editor.commit()
    }

    fun clearPrefs() {
        setToken("")
        setUserLogged("")
    }

    fun setRecordedVoiceNote(newValue: MutableSet<String>?) {
        val editor = sharedPreferences.edit()
        editor.putStringSet(getUserLogged(), newValue)
        editor.commit()
    }

    fun getRecordedVoiceNotes(): MutableSet<String>? {
        return sharedPreferences.getStringSet(
            getUserLogged(),
            mutableSetOf()
        )
    }
}