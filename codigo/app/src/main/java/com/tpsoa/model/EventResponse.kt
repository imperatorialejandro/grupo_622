package com.tpsoa.model

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("state")
    val state: String,
    @SerializedName("event")
    val event: Event,
    @SerializedName("msg")
    val message: String
)