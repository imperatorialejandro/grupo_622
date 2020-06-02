package com.tpsoa.model

import com.google.gson.annotations.SerializedName

data class SignInResponse(
    @SerializedName("state")
    val state: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String
)