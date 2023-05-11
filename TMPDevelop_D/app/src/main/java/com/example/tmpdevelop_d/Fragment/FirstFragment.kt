package com.example.tmpdevelop_d.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tmpdevelop_d.Adapter.GroupAdapter
import com.example.tmpdevelop_d.Dialog.CreateGroupDialog
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.Group
import com.example.tmpdevelop_d.chatroom.ChatRoomActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FirstFragment : Fragment(), GroupAdapter.ItemClickListener {



    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val groups = arrayListOf<Group>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 設定佈局
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        // 綁定 RecyclerView
        recyclerView = view.findViewById(R.id.group_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // 初始化 GroupAdapter 並設置點擊事件
        adapter = GroupAdapter(groups, this)
        recyclerView.adapter = adapter

        // 綁定 FloatingActionButton
        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            // 點擊 FloatingActionButton 後顯示創建群組的 Dialog
            showCreateGroupDialog()
        }

        // 綁定 SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        // 設定 SwipeRefreshLayout 的刷新監聽器
        swipeRefreshLayout.setOnRefreshListener {
            fetchGroupListAndUpdateRecyclerView {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 獲取群組列表並更新 RecyclerView
        fetchGroupListAndUpdateRecyclerView()

        return view
    }

    // 從 Firestore 中獲取群組列表並更新 RecyclerView
    private fun fetchGroupListAndUpdateRecyclerView(onComplete: (() -> Unit)? = null) {
        val db = Firebase.firestore
        val groupsRef = db.collection("Groups")
        // 獲取當前用戶 ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        groups.clear() // 清空群組列表
        groupsRef.get().addOnSuccessListener { result ->
            for (document in result) {
                // 將 document id 設置為 groupId
                val group = document.toObject(Group::class.java).apply {
                    id = document.id
                }
                // 如果當前用戶是群組成員，則添加到群組列表中
                if (group.memberIds.contains(currentUserId)) {
                    groups.add(group)
                }
            }
            // 通知 Adapter 數據已更改
            adapter.notifyDataSetChanged()
            onComplete?.invoke()
        }.addOnFailureListener { exception ->
            Log.d(Companion.TAG, "Error getting documents: ", exception)
            onComplete?.invoke()
        }
    }

    // 顯示創建群組的 Dialog
    private fun showCreateGroupDialog() {
        val createGroupDialog = CreateGroupDialog()
        createGroupDialog.show(childFragmentManager, "createGroupDialog")
    }

    override fun onItemClick(group: Group) {
        // 啟動 ChatRoomActivity，並將選中的群組 ID 傳遞給 ChatRoomActivity
        val intent = Intent(activity, ChatRoomActivity::class.java)
        intent.putExtra("groupId", group.id)
        startActivity(intent)
    }
    companion object {
        private const val TAG = "FirstFragment"
    }
}