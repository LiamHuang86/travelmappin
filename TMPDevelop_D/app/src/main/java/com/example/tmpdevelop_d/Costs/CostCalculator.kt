package com.example.tmpdevelop_d.Costs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import androidx.lifecycle.ViewModel

class CostCalculator : ViewModel() {

    private val TAG = "CostCalculator"

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("CostInfo")
    private val avgCostRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("AverageCost")

    private val _averageCostListLiveData = MutableLiveData<List<AverageCost>>()
    val averageCostListLiveData: LiveData<List<AverageCost>> = _averageCostListLiveData



    fun calculateAverageCosts() {
        Log.d(TAG, "calculateAverageCosts called")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val averageCostList = mutableListOf<AverageCost>()
                var totalAmount = 0.0

                dataSnapshot.children.forEach { costSnapshot ->
                    val cost = costSnapshot.getValue(Cost::class.java)

                    // 如果 expense 為 null 或 0，則不進行抓取
                    if (cost?.expense != null && cost.expense > 0) {
                        val payerId = cost.payerId
                        val expense = cost.expense
                        val userCount = cost.userCount
                        val averageCost = expense.toDouble() / userCount.toDouble()

                        cost.friendInfoList.forEach { user ->
                            val uid = user.id
                            val amount = if (uid == payerId) {
                                averageCost * (userCount - 1)
                            } else {
                                -averageCost
                            }.toDouble()

                            // 將計算後的資料加入列表
                            val payerName = cost.payerName
                            val placeName = cost.placeName
                            averageCostList.add(AverageCost(uid, amount, payerName, placeName))

                            totalAmount += amount

                            Log.d(TAG, "User: $uid, Amount: $amount, PayerName: $payerName, PlaceName: $placeName")

                        }
                    }
                }

                // 寫入 Firebase Realtime Database
                avgCostRef.setValue(averageCostList)

                // 發佈更新事件
                _averageCostListLiveData.postValue(averageCostList)
                Log.d(TAG, "averageCostListLiveData updated with ${averageCostList.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                // 處理錯誤
                Log.e(TAG, "onCancelled: ${error.message}")

            }
        })
    }
}