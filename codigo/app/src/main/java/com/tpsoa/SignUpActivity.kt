package com.tpsoa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.tpsoa.common.Utils
import com.tpsoa.model.SignUpRequest
import com.tpsoa.rest.ApiInterface
import com.tpsoa.rest.ServiceBuilder
import com.tpsoa.rest.SignUpResponse
import kotlinx.android.synthetic.main.activity_login.password_text
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        name_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validateName()
            }
        }

        name_text.addTextChangedListener(addTextWatcherButtonState())

        lastname_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validateLastName()
            }
        }

        lastname_text.addTextChangedListener(addTextWatcherButtonState())

        dni_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validateDni()
            }
        }

        dni_text.addTextChangedListener(addTextWatcherButtonState())

        group_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validateGroup()
            }
        }

        group_text.addTextChangedListener(addTextWatcherButtonState())

        commission_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validateCommission()
            }
        }

        commission_text.addTextChangedListener(addTextWatcherButtonState())

        email_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validateEmail()
            }
        }

        email_text.addTextChangedListener(addTextWatcherButtonState())

        password_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validatePassword()
            }
        }

        password_text.addTextChangedListener(addTextWatcherButtonState())

        password_confirmation_text.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                validatePasswordConfirmation()
            }
        }

        password_confirmation_text.addTextChangedListener(addTextWatcherButtonState())
    }

    private fun addTextWatcherButtonState() : TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                registration_btn.isEnabled = checkSignUpButtonState()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
    }

    private fun validateName() {
        val name = name_text!!.text.toString()
        if (name.isEmpty()) {
            name_text!!.error = "Could not be empty"
        } else {
            name_text!!.error = null
        }
    }

    private fun validateLastName() {
        val lastname = lastname_text!!.text.toString()
        if (lastname.isEmpty()) {
            lastname_text!!.error = "Could not be empty"
        } else {
            lastname_text!!.error = null
        }
    }

    private fun validateDni() {
        val dni = dni_text!!.text.toString()
        if (!dni.isDigitsOnly()) {
            dni_text!!.error = "Only numbers allowed"
        } else {
            dni_text!!.error = null
        }
    }

    private fun validateGroup() {
        val group = group_text!!.text.toString()
        if (!group.isDigitsOnly()) {
            group_text!!.error = "Only numbers allowed"
        } else {
            group_text!!.error = null
        }
    }

    private fun validateCommission() {
        val commission = commission_text!!.text.toString()
        if (!commission.isDigitsOnly()) {
            commission_text!!.error = "Only numbers allowed"
        } else {
            commission_text!!.error = null
        }
    }

    private fun validateEmail() {
        val email = email_text!!.text.toString()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_text!!.error = "Enter a valid email address"
        } else {
            email_text!!.error = null
        }
    }

    private fun validatePassword() {
        val password = password_text!!.text.toString()
        if (password.length < 8) {
            password_text!!.error = "Should be at least 8 characters"
        } else {
            password_text!!.error = null
        }
    }

    private fun validatePasswordConfirmation() {
        val password = password_text!!.text.toString()
        val passwordConfirmation = password_confirmation_text!!.text.toString()
        if (passwordConfirmation.length < 8) {
            password_confirmation_text!!.error = "Should be at least 8 characters"
        } else {
            password_confirmation_text!!.error = null
        }
        if (password != passwordConfirmation) {
            password_confirmation_text!!.error = "Passwords did not match"
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
        val passwordConfirmation = password_confirmation_text!!.text.toString()

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

    fun onRegistrationClick(v: View) {
        if(!Utils.isOnline(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        registration_btn.isEnabled = false

        val name = name_text!!.text.toString()
        val lastname = lastname_text!!.text.toString()
        val dni = dni_text!!.text.toString().toInt()
        val group = group_text!!.text.toString().toInt()
        val commission = commission_text!!.text.toString().toInt()
        val email = email_text!!.text.toString()
        val password = password_text!!.text.toString()

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val req = SignUpRequest(name, lastname, dni, group, commission, email, password)
        val call = request.SignUp(req)

        call.enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful){
                    onSignUpSuccess()
                } else {
                    val res = response.errorBody() as SignUpResponse
                    onSignUpFailed(res.msg)
                }
                registration_btn.isEnabled = true
            }
            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                onSignUpFailed("${t.message}")
            }
        })

    }

    fun onLoginClick(v: View) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
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
