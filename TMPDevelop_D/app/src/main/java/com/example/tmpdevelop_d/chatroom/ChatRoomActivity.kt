package com.example.tmpdevelop_d.chatroom

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmpdevelop_d.Adapter.ChatAdapter
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.Group
import com.example.tmpdevelop_d.Users.Message
import com.example.tmpdevelop_d.Users.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var groupNameTextView: TextView
    private lateinit var totalMembersTextView: TextView
    private lateinit var settingsButton: ImageButton
    private val messages = arrayListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // 初始化 RecyclerView 以顯示聊天消息
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        // 設置適配器以顯示消息
        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.adapter = chatAdapter

        // 初始化 EditText 和 Button，讓用戶輸入和發送消息
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_button)

        // 初始化顯示群組名稱和成員數量的 TextView
        groupNameTextView = findViewById(R.id.group_name_text_view)
        totalMembersTextView = findViewById(R.id.total_members_text_view)

        // 初始化聊天室設置按鈕
        settingsButton = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            // 在這裡添加聊天室設置的功能
        }

        // 設置發送按鈕的點擊事件監聽器
        sendButton.setOnClickListener {
            sendMessage()
        }

        // 獲取聊天室內的消息並更新 RecyclerView
        fetchMessagesAndUpdateRecyclerView()

        // 獲取群組信息並更新群組名稱和成員數量
        fetchGroupInfoAndUpdateTextViews()
    }

    private fun fetchMessagesAndUpdateRecyclerView() {
        val groupId = intent.getStringExtra("groupId")!!
        val db = Firebase.firestore
        val messagesRef = db.collection("GroupMessages").document(groupId).collection("Messages")

        messagesRef.orderBy("timestamp").addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.d(ContentValues.TAG, "Error getting messages: ", exception)
                return@addSnapshotListener
            }

            messages.clear()
            snapshot?.let {
                for (document in it) {
                    val message = document.toObject(Message::class.java)
                    messages.add(message)
                }
                chatAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messages.size - 1) // 将RecyclerView滚动到最底部
                Log.d(ContentValues.TAG, "Fetched messages: $messages")
            }
        }
    }

    private fun fetchGroupInfoAndUpdateTextViews() {
        val groupId = intent.getStringExtra("groupId")!!
        val db = Firebase.firestore
        val groupRef = db.collection("Groups").document(groupId)

        groupRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val group = document.toObject(Group::class.java)
                groupNameTextView.text = group?.groupName
                totalMembersTextView.text = getString(R.string.totalmembers, group?.memberIds?.size)
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Error getting group info: ", exception)
        }
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val groupId = intent.getStringExtra("groupId")
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null && groupId != null) {
                val db = Firebase.firestore
                val messagesRef =
                    db.collection("GroupMessages").document(groupId).collection("Messages")

                // 根據 Auth uid 查找 Firestore 中的 'Users' 集合中匹配的用戶
                val usersRef = db.collection("Users")
                usersRef.whereEqualTo("uid", currentUserId).get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val user = document.toObject(Users::class.java)
                            // 使用找到的用戶對象獲取姓名和頭像 URL
                            val senderName = user.username
                            val senderImageUrl = user.imageUrl

                            // 創建新的消息並添加到 Firestore
                            val newMessage = Message(
                                senderName = senderName,
                                senderId = currentUserId,
                                senderImageUrl = senderImageUrl,
                                text = messageText,
                                timestamp = System.currentTimeMillis()
                            )
                            messagesRef.orderBy("timestamp")
                                .addSnapshotListener { snapshot, exception ->
                                    if (exception != null) {
                                        Log.d(
                                            ContentValues.TAG,
                                            "Error getting messages: ",
                                            exception
                                        )
                                        return@addSnapshotListener
                                    }

                                    snapshot?.let { querySnapshot ->
                                        val newMessages = mutableListOf<Message>()

                                        for (document in querySnapshot) {
                                            val message = document.toObject(Message::class.java)
                                            newMessages.add(message)
                                        }

                                        messages.addAll(0, newMessages)
                                        chatAdapter.notifyDataSetChanged()

                                        chatRecyclerView.post {
                                            chatRecyclerView.scrollToPosition(0)
                                        }

                                        Log.d(ContentValues.TAG, "Fetched messages: $messages")
                                    }
                                }
                        }
                    }
            }
        }
    }
}