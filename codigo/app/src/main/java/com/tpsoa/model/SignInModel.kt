package com.tpsoa.model

import com.tpsoa.BuildConfig

data class SignInRequest(
    val email: String,
    val password: String
) {
    val env: String = BuildConfig.API_ENV
}