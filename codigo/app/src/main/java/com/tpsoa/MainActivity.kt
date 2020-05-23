package com.tpsoa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tpsoa.sharedpreferences.SharedPreferencesManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLogged()
    }

    private fun checkLogged() {
        if (!gSharedPreferencesManager.isLogged(applicationContext)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
