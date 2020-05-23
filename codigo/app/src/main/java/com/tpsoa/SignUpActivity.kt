package com.tpsoa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.tpsoa.model.SignUpRequest
import com.tpsoa.rest.ApiInterface
import com.tpsoa.rest.ServiceBuilder
import com.tpsoa.rest.SignInResponse
import com.tpsoa.rest.SignUpResponse
import kotlinx.android.synthetic.main.activity_login.password_text
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        name_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }
        lastname_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }

        dni_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }

        group_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }

        commission_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }

        email_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }

        password_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }

        password_confirmation_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }
        }

    }

    private fun checkSignUpButtonState(): Boolean {
        val name = name_text!!.text.toString()
        val lastname = lastname_text!!.text.toString()
        val dni = dni_text!!.text.toString()
        val group = group_text!!.text.toString()
        val commission = commission_text!!.text.toString()
        val email = email_text!!.text.toString()
        val password = password_text!!.text.toString()
        val passwordConfirmation = password_text!!.text.toString()

        return name.isNotEmpty() &&
                lastname.isNotEmpty() &&
                dni.isDigitsOnly() &&
                group.isDigitsOnly() &&
                commission.isDigitsOnly() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && password.isNotEmpty()
                && passwordConfirmation.isNotEmpty()
                && password == passwordConfirmation
    }

    fun onClick(v: View) {
        val name = name_text!!.text.toString()
        val lastname = lastname_text!!.text.toString()
        val dni = dni_text!!.text.toString().toInt()
        val group = group_text!!.text.toString().toInt()
        val commission = commission_text!!.text.toString().toInt()
        val email = email_text!!.text.toString()
        val password = password_text!!.text.toString()

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val req = SignUpRequest(name, lastname, dni, group, commission, email, password)
        // val req = SignUpRequest("Alejandro", "Imperatori", 40011139 , 622, 2900, "aimperatori@alumno.unlam.edu.ar", "40011139")
        val call = request.SignUp(req)

        call.enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                val res = response.body() as SignUpResponse
                if (response.isSuccessful){
                    onSignUpSuccess()
                } else {
                    onSignUpFailed(res.msg)
                }
            }
            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                onSignUpFailed("${t.message}")
            }
        })

    }

    private fun onSignUpSuccess() {
        Toast.makeText(this, "Sign up successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun onSignUpFailed(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

}
