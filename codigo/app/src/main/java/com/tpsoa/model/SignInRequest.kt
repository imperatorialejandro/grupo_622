package com.tpsoa.model

import com.google.gson.annotations.SerializedName
import com.tpsoa.BuildConfig

data class SignInRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
) {
    @SerializedName("env")
    val env: String = BuildConfig.API_ENV
}