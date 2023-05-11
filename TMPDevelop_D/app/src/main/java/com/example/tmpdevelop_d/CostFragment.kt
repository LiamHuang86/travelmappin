package com.example.tmpdevelop_d

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmpdevelop_d.Adapter.CostFragmentRecyclerViewAdapter
import com.example.tmpdevelop_d.Costs.AverageCost
import com.example.tmpdevelop_d.Costs.CostCalculator
import com.google.firebase.auth.FirebaseAuth



class CostFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CostFragmentRecyclerViewAdapter
    private lateinit var totalAmountTextView: TextView

    private val viewModel: CostCalculator by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CostFragment", "onCreateView")
        val view = inflater.inflate(R.layout.fragment_cost, container, false)
        recyclerView = view.findViewById(R.id.CostRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = CostFragmentRecyclerViewAdapter()
        recyclerView.adapter = adapter
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("CostFragment", "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        // 計算平均消費
        viewModel.calculateAverageCosts()

        // 添加對 LiveData 的觀察
        viewModel.averageCostListLiveData.observe(viewLifecycleOwner) { averageCostList ->
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            val filteredList = averageCostList.filter { it.uid == currentUserUid }.sortedByDescending { it.timestamp }
            // 更新 RecyclerView
            adapter.setData(filteredList)

            // 计算总金额并更新 totalAmountTextView
            val totalAmount = calculateTotalAmount(filteredList)
            totalAmountTextView.text = "$ ${String.format("%.2f", totalAmount)}"
        }

        // 設置 RecyclerView 的點擊事件
        adapter.setOnItemClickListener(object :
            CostFragmentRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(item: AverageCost) {
                Log.d("CostFragment", "RecyclerView item clicked: ${item.placeName}")
                // 使用 AverageCost 對象的信息更新對話框
                val title = SpannableString(item.placeName)
                if (title != null) {
                    title.setSpan(
                        ForegroundColorSpan(Color.BLACK),
                        0,
                        title.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                val message =
                    SpannableString(
                        "付款人: ${item.payerName}\n地點: ${item.placeName}\n應付金額: ${String.format("%.2f", item.amount)}"
                    )
                message.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    0,
                    message.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("確認", null)
                    .create()
                dialog.window?.setBackgroundDrawableResource(android.R.color.white)
                dialog.show()
            }
        })
    }

    private fun calculateTotalAmount(averageCostList: List<AverageCost>): Double {
        Log.d("CostFragment", "calculateTotalAmount: input list size = ${averageCostList.size}")
        var totalAmount = 0.0
        averageCostList.forEach { averageCost ->
            totalAmount += averageCost.amount
        }
        Log.d("CostFragment", "calculateTotalAmount: result = $totalAmount")
        return totalAmount
    }
}
