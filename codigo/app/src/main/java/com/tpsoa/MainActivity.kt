package com.tpsoa

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tpsoa.sharedpreferences.SharedPreferencesManager

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLogged()
    }

    private fun checkLogged() {
        if (!SharedPreferencesManager.isLogged(applicationContext)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            setContentView(R.layout.activity_main)
        }
    }

    fun onLogoutClick(view: View) {
        SharedPreferencesManager.clear(applicationContext)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}
