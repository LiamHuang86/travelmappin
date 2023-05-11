package com.example.tmpdevelop_d.Costs

import com.google.firebase.firestore.auth.User

data class AverageCost(
    var uid: String? = null,
    var amount: Double = 0.0,
    var payerName: String? = null,
    var placeName: String? = null,
    var timestamp: Int = 0,
    var averageCost: Double = 0.0,
    var groupName: String? = null,
    var expense: Int = 0,
    var date: String? = null,
    var hour: Int = 0,
    var minute: Int = 0,
    var friendInfoList: List<Users> = emptyList(),
    var placeId: String? = null
) {
    constructor() : this(null, 0.0, null, null,  0,0.0, null, 0, null, 0, 0, emptyList(),null)
}
