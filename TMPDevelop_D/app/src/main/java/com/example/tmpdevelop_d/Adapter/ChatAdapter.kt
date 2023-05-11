package com.example.tmpdevelop_d.Adapter

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.Message
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

// 聊天適配器，用於在聊天室中顯示消息列表
class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    // 聲明兩個消息類型常量，分別代表已發送的消息和已接收的消息
    companion object {
        private const val MESSAGE_TYPE_SENT = 0
        private const val MESSAGE_TYPE_RECEIVED = 1
    }

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    // 根據消息的發送者是當前用戶還是其他用戶，返回相應的類型值
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            MESSAGE_TYPE_SENT
        } else {
            MESSAGE_TYPE_RECEIVED
        }
    }

    // 根據消息類型創建相應的視圖
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = if (viewType == MESSAGE_TYPE_SENT) {
            layoutInflater.inflate(R.layout.item_message_sent, parent, false)
        } else {
            layoutInflater.inflate(R.layout.item_message_received, parent, false)
        }
        return ChatViewHolder(view)
    }

    // 綁定視圖和數據
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    // 返回消息數量
    override fun getItemCount(): Int = messages.size

    // 聊天視圖持有者類
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.message_text_view)
        var timestamp: TextView = itemView.findViewById(R.id.timestamp_text_view)
        var senderImageUrl: CircleImageView? = itemView.findViewById(R.id.sender_avatar)
        var senderName: TextView? = itemView.findViewById(R.id.sender_name_text_view)

        // 綁定數據到視圖
        fun bind(message: Message) {
            text.text = message.text
            timestamp.text =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

            // 如果是接收到的消息，則顯示發送者頭像和姓名
            if (getItemViewType(adapterPosition) == MESSAGE_TYPE_RECEIVED) {
                senderImageUrl?.let {
                    Glide.with(itemView)
                        .load(message.senderImageUrl)
                        .into(it)
                }
                senderName?.text = message.senderName
            }
        }
    }
}