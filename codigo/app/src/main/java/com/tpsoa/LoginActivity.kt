package com.tpsoa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tpsoa.model.SignInRequest
import com.tpsoa.rest.ApiInterface
import com.tpsoa.rest.ServiceBuilder
import com.tpsoa.rest.SignInResponse
import com.tpsoa.sharedpreferences.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        user_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validateEmail()
                login_btn.isEnabled = checkLoginButtonState()
            }
        }

        password_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validatePassword()
                login_btn.isEnabled = checkLoginButtonState()
            }
        }
    }

    fun onSignUpClick(v: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    fun onLoginClick(v: View) {
        login_btn.isEnabled = false

        val email = user_text!!.text.toString()
        val password = password_text!!.text.toString()

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val req = SignInRequest(email, password)
        val call = request.SignIn(req)

        call.enqueue(object : Callback<SignInResponse>{
            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {
                if (response.isSuccessful){
                    val res = response.body() as SignInResponse
                    onLoginSuccess(res.token)
                } else {
                    onLoginFailed()
                }
                login_btn.isEnabled = true
            }
            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                onLoginFailed("${t.message}")
            }
        })
    }

    private fun onLoginSuccess(token: String) {
        Toast.makeText(this, "Sign in successfully", Toast.LENGTH_SHORT).show()
        SharedPreferencesManager.setLogged(applicationContext, true)
        SharedPreferencesManager.setToken(applicationContext, token)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun onLoginFailed(message: String = "Incorrect email or password") {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateEmail() {
        val email = user_text!!.text.toString()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            user_text!!.error = "Enter a valid email address"
        } else {
            user_text!!.error = null
        }
    }

    private fun validatePassword() {
        val password = password_text!!.text.toString()
        if (password.isEmpty()) {
            password_text!!.error = "Could not be empty"
        } else {
            password_text!!.error = null
        }
    }

    private fun checkLoginButtonState(): Boolean {
        val email = user_text!!.text.toString()
        val password = password_text!!.text.toString()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isNotEmpty()
    }

}
