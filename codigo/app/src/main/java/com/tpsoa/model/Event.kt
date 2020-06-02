package com.tpsoa.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val id: Int,
    @SerializedName("dni")
    val dni: Int,
    @SerializedName("type_events")
    val type: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("description")
    val description: String
)