package com.tpsoa

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.tpsoa.sharedpreferences.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLogged()

        setContentView(R.layout.activity_main)

        var loggedUser = SharedPreferencesManager.getUserLogged(applicationContext)
        email_user_text.text = "Hello $loggedUser!"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.voice_recorder_menu, menu)
        return true
    }

    private fun checkLogged() {
        if (!isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_item_menu -> {
                onLogoutClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isLogged(): Boolean {
        return SharedPreferencesManager.getUserLogged(applicationContext) != ""
    }

    private fun onLogoutClick() {
        SharedPreferencesManager.clearPrefs(applicationContext)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}
