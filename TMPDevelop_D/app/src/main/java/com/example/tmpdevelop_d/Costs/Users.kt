package com.example.tmpdevelop_d.Costs

data class Users(
    val userID: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val id: String? = null
) {
    constructor() : this("", "", null, null)
}