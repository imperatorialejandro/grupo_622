package com.tpsoa.model

import com.google.gson.annotations.SerializedName
import com.tpsoa.BuildConfig

data class EventRequest(
    @SerializedName("type_events")
    val type: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("description")
    val description: String
) {
    @SerializedName("env")
    val env: String = BuildConfig.API_ENV
}