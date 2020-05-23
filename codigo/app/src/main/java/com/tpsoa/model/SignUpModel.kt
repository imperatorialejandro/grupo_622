package com.tpsoa.model

import com.tpsoa.BuildConfig

data class SignUpRequest(
    val name: String,
    val lastname: String,
    val dni: Int,
    val group: Int,
    val commission: Int,
    val email: String,
    val password: String
) {
    val env: String = BuildConfig.API_ENV
}