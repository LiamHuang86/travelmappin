package com.example.tmpdevelop_d.Costs

import com.google.firebase.database.DataSnapshot


data class Cost(
    var date: String? = null,
    var expense: Int = 0,
    var friendInfoList: List<Users> = emptyList(),
    var groupName: String? = null,
    var hour: Int = 0,
    var itemName: String? = null,
    var iconIndex: Int = 0,
    var markerIndex: Int = 0,
    var minute: Int = 0,
    var payerId: String? = null,
    var payerName: String? = null,
    var placeName: String? = null,
    var routeIndex: Int = 0,
    var userCount: Int = 0,
    var location: DataSnapshot? = null,
    var id: String? = null,





    ) {
    constructor() : this(null, 0,emptyList(), null, 0, null, 0, 0, 0, null, null,null,
        0,0,null,null)
}