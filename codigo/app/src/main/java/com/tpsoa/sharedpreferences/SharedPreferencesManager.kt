package com.tpsoa.sharedpreferences

import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesManager {
    private const val APP_SETTINGS = "com.tpsoa.voicerecorder"

    private const val USER_LOGGER = "user_logged"
    private const val TOKEN = "token"
    private const val RECORDEDVOICENOTE = "recordedVoiceNote"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            APP_SETTINGS,
            Context.MODE_PRIVATE
        )
    }

    fun getToken(context: Context): String? {
        return getSharedPreferences(context)
            .getString(TOKEN, "")
    }

    fun setToken(context: Context, newValue: String) {
        val editor =
            getSharedPreferences(context).edit()
        editor.putString(TOKEN, newValue)
        editor.commit()
    }

    fun getUserLogged(context: Context): String? {
        return getSharedPreferences(context)
            .getString(USER_LOGGER, "")
    }

    fun setUserLogged(context: Context, newValue: String) {
        val editor =
            getSharedPreferences(context).edit()
        editor.putString(USER_LOGGER, newValue)
        editor.commit()
    }

    fun clearPrefs(context: Context) {
        var editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.apply()
    }

    fun setRecordedVoiceNote(context: Context, newValue: Set<String>) {
        val editor =
            getSharedPreferences(context).edit()
        editor.putStringSet(TOKEN, newValue)
        editor.commit()
    }
}