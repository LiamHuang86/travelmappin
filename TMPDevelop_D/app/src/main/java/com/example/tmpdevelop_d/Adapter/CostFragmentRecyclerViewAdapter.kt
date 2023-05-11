package com.example.tmpdevelop_d.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tmpdevelop_d.Costs.AverageCost
import com.example.tmpdevelop_d.R
import com.google.firebase.auth.FirebaseAuth

class CostFragmentRecyclerViewAdapter : RecyclerView.Adapter<CostFragmentRecyclerViewAdapter.ViewHolder>() {

    private val data = mutableListOf<AverageCost>()
    private var listener: OnItemClickListener? = null

    fun setData(list: List<AverageCost>) {
        data.clear()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        data.addAll(list.filter { it.uid == currentUserUid }.sortedByDescending { it.timestamp })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_cost, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.payerName.text = item.payerName
        holder.placeName.text = item.placeName
        holder.amount.text = String.format("%.2f", item.amount)
        // set color based on the value of amount
        if (item.amount >= 0) {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.blue))
        } else {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        }
        holder.itemView.setOnClickListener{
            listener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val payerName: TextView = itemView.findViewById(R.id.payerName)
        val placeName: TextView = itemView.findViewById(R.id.placeName)
        val amount: TextView = itemView.findViewById(R.id.amount)
    }

    interface OnItemClickListener {
        fun onItemClick(item: AverageCost)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }
}