package com.example.tmpdevelop_d.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tmpdevelop_d.R

class CostInfoAdapter(private val dataList: List<Triple<String, String, Int>>)
    : RecyclerView.Adapter<CostInfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeNameView: TextView = view.findViewById(R.id.place_Name_Text)
        val dateView: TextView = view.findViewById(R.id.date_Text)
        val expenseView: TextView = view.findViewById(R.id.expense_Text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_cost, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.placeNameView.text = item.first
        holder.dateView.text = item.second
        holder.expenseView.text = item.third.toString()
    }

    override fun getItemCount(): Int = dataList.size
}