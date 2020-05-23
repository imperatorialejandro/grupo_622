package com.tpsoa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.tpsoa.model.SignInRequest
import com.tpsoa.rest.ApiInterface
import com.tpsoa.rest.ServiceBuilder
import com.tpsoa.rest.SignResponse
import kotlinx.android.synthetic.main.login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        login_btn.setOnClickListener{
            login()
        }

        val button = findViewById<TextView>(R.id.sign_up_btn)
        button.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        /*sign_up_btn.setOnClickListener{
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
            //showSignUp()
        }*/
    }

    private fun login() {
        if (!validateLogin()) {
            return
        }

        val email = user_text!!.text.toString()
        val password = password_text!!.text.toString()

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val req = SignInRequest(email, password)
        val call = request.SignIn(req)

        call.enqueue(object : Callback<SignResponse>{
            override fun onResponse(call: Call<SignResponse>, response: Response<SignResponse>) {
                if (response.isSuccessful){
                    Toast.makeText(applicationContext, "Sign in successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Email or password incorrect ", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<SignResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun validateLogin(): Boolean {
        var valid = true

        val email = user_text!!.text.toString()
        val password = password_text!!.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            user_text!!.error = "Enter a valid email address"
            valid = false
        } else {
            user_text!!.error = null
        }

        if (password.isEmpty() || password.length < 8) {
            password_text!!.error = "Must be at least 8 characters"
            valid = false
        } else {
            password_text!!.error = null
        }

        return valid
    }

    /*private fun showSignUp() {
        sign_up_layout.visibility=View.VISIBLE
        home_layout.visibility=View.GONE
    }

    private fun showHome() {
        sign_up_layout.visibility=View.GONE
        home_layout.visibility=View.VISIBLE
    }*/

}
