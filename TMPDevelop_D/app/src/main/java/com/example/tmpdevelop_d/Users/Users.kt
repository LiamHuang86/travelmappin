package com.example.tmpdevelop_d.Users

data class Users(
    val userID: String = "",
    val username: String = "",
    val imageUrl: String? = null,
    val uid: String? = null
) {
    constructor() : this("", "", null, null)
}