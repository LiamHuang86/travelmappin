package com.example.tmpdevelop_d.Fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmpdevelop_d.Adapter.CostInfoAdapter
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.CostInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ThirdFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_third, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.costrecyclerview)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // 創建Firebase資料庫的參考
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("CostInfo")

        // 監聽Firebase資料庫中的CostInfo節點
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 將CostInfo節點轉換成一個List<CostInfo>物件
                val costInfoList = mutableListOf<CostInfo>()
                for (postSnapshot in dataSnapshot.children) {
                    val costInfo = postSnapshot.getValue(CostInfo::class.java)
                    costInfo?.let { costInfoList.add(it) }
                }
                // 取得每個CostInfo物件的placeName、date、expense欄位
                val displayList = costInfoList.map { costInfo: CostInfo ->
                    Triple(costInfo.placeName ?: "", costInfo.date ?: "", costInfo.expense)
                }
                // 將資料傳遞給RecyclerViewAdapter並顯示在RecyclerView中
                val adapter = CostInfoAdapter(displayList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // 錯誤處理
            }
        })

        return view
    }
}