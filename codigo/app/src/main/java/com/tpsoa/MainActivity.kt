package com.tpsoa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        login_btn.setOnClickListener{
            var status = "Logged In successfully"
            Toast.makeText(this.getApplicationContext(), status, Toast.LENGTH_SHORT).show()
        }
    }
}
