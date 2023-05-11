package com.example.tmpdevelop_d.Users

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.UserInfo

data class CostInfo(var routeIndex: Int = 0,
                    var markerIndex: Int = 0,
                    var iconIndex: Int = 0,
                    var placeName: String? = null,
                    var date: String? = null,
                    var hour: Int = 0,
                    var minute: Int = 0,
                    var groupinfo: Group? = null,
                    var friendInfoList: List<Users> = emptyList(),
                    var payer: UserInfo? = null,
                    var itemName: String? = null,
                    var expense: Int = 0,
                    var location: LatLng? = null){

    constructor() : this(0, 0, 0, null, null, 0, 0, null, emptyList(), null, null, 0, null)
}
