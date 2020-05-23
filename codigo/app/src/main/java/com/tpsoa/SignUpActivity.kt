package com.tpsoa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.tpsoa.model.SignInRequest
import com.tpsoa.model.SignUpRequest
import com.tpsoa.rest.ApiInterface
import com.tpsoa.rest.ServiceBuilder
import com.tpsoa.rest.SignResponse
import com.tpsoa.rest.SignUpResponse
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.login.password_text
import kotlinx.android.synthetic.main.sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        registration_btn.setOnClickListener{
            Toast.makeText(applicationContext, "asdqwe", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signUp() {
        if (!validateRegistration()) {
            return
        }

        val name = name_text!!.text.toString()
        val lastname = lastname_text!!.text.toString()
        val dni = dni_text!!.text.toString().toInt()
        val group = group_text!!.text.toString().toInt()
        val commission = commission_text!!.text.toString().toInt()
        val email = email_text!!.text.toString()
        val password = password_text!!.text.toString()

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val req = SignUpRequest(name, lastname, dni, group, commission, email, password)
        val call = request.Register(req)

        call.enqueue(object : Callback<SignUpResponse>{
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful){
                    Toast.makeText(applicationContext, "Sign up successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Error in params", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun validateRegistration(): Boolean {
        /*var valid = true

        val email = user_text!!.text.toString()
        val password = password_text!!.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            user_text!!.error = "Enter a valid email address"
            valid = false
        } else {
            user_text!!.error = null
        }

        if (password.isEmpty() || password.length < 8 || password.length > 20) {
            password_text!!.error = "between 8 and 20 alphanumeric characters"
            valid = false
        } else {
            password_text!!.error = null
        }
        */
        return true
    }

}
