package com.tpsoa.sharedpreferences

import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesManager {
    private const val APP_SETTINGS = "com.tpsoa.voicerecorder"

    private const val IS_LOGGED = "is_logged"
    private const val TOKEN = "token"

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

    fun isLogged(context: Context): Boolean {
        return getSharedPreferences(context)
            .getBoolean(IS_LOGGED, false)
    }

    fun setLogged(context: Context, newValue: Boolean) {
        val editor =
            getSharedPreferences(context).edit()
        editor.putBoolean(IS_LOGGED, newValue)
        editor.commit()
    }

    fun clear(context: Context) {
        var editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}