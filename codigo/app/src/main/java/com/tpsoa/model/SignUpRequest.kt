package com.tpsoa.model

import com.google.gson.annotations.SerializedName
import com.tpsoa.BuildConfig

data class SignUpRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("lastname")
    val lastname: String,
    @SerializedName("dni")
    val dni: Int,
    @SerializedName("group")
    val group: Int,
    @SerializedName("commission")
    val commission: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
) {
    @SerializedName("env")
    val env: String = BuildConfig.API_ENV
}