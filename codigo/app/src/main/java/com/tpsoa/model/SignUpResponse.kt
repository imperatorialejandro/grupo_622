package com.tpsoa.model

import com.google.gson.annotations.SerializedName

data class SignUpResponse(
    @SerializedName("state")
    val state: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("msg")
    val message: String
)
