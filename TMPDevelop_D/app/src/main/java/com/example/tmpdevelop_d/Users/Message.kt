package com.example.tmpdevelop_d.Users

data class Message(
    val senderName: String ="",
    val senderId: String = "",
    val senderImageUrl: String? = null,
    val text: String = "",
    val timestamp: Long = 0L
) {
    constructor() : this("","",null, "", 0L)
}