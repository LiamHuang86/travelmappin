package com.example.tmpdevelop_d.Costs

data class AverageCost (
    var uid: String? = null,
    var amount: Double = 0.0,
    var payerName: String? = null,
    var placeName: String? = null,
    var timestamp: Long = 0,
    var averageCost: Double = 0.0,


    ){
    constructor() : this(null,0.0,null,null,0,0.0)
}