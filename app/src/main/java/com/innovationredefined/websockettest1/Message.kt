package com.innovationredefined.websockettest1

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("type") val type: String,
    @SerializedName("senderUserProfileId") val senderUserProfileId: String = EMPTY_UUID_STRING,
    @SerializedName("recipientUserProfileId") val recipientUserProfileId: String = EMPTY_UUID_STRING,
    @SerializedName("text") val text: String,
    @SerializedName("timeSent") val timeSent: Long = 0
)
