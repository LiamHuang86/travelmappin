package com.example.tmpdevelop_d.Users

data class Group(
    var id: String = "",
    val groupName: String = "",
    val creatorId: String = "",
    val photoUrl: String = "",
    val memberIds: List<String> = emptyList(),
    val totalMembers: Int = 0
) {
    constructor() : this("","", "", "", emptyList(), 0)
}