package com.example.tmpdevelop_d.Dialog

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.Group
import com.example.tmpdevelop_d.Users.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


private const val DEFAULT_GROUP_IMAGE_URL = "https://your-default-image-url.com"

class CreateGroupDialog : DialogFragment() {



    private lateinit var groupNameEditText: EditText
    private lateinit var groupImageView: ImageView
    private lateinit var addRecyclerView: RecyclerView
    private lateinit var finishButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var userList: List<Users>
    private lateinit var selectedIds: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_create_group, container, false)

        groupNameEditText = view.findViewById(R.id.group_name_edit_text)
        groupImageView = view.findViewById(R.id.image_group)
        addRecyclerView = view.findViewById(R.id.add_recycler_view)
        finishButton = view.findViewById(R.id.finish_button)
        progressBar = view.findViewById(R.id.progress_bar)

        selectedIds = mutableListOf()

        // 設置選擇團隊封面照片的點擊事件
        groupImageView.setOnClickListener {
            // 啟動相冊選擇圖片
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // 設置完成按鈕的點擊事件
        finishButton.setOnClickListener {
            val groupName = groupNameEditText.text.toString()
            val creatorId = FirebaseAuth.getInstance().currentUser?.uid
            val photoUrl = groupImageUri?.toString() ?: DEFAULT_GROUP_IMAGE_URL
            val memberIds = selectedIds.toList()
            val totalMembers = System.currentTimeMillis().toInt()

            if (groupName.isNotEmpty() && creatorId != null) {
                progressBar.visibility = View.VISIBLE
                val group = Group("",groupName, creatorId, photoUrl, memberIds, totalMembers)
                FirestoreRepository.createGroup(group) { groupId ->
                    progressBar.visibility = View.GONE
                    if (groupId != null) {
                        dismiss()
                        Toast.makeText(context, "Group created successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to create group", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 加載好友列表
        FirestoreRepository.getUsers { users ->
            userList = users
            val adapter = AddMemberAdapter(userList, object : OnUserClickListener {
                override fun onUserClick(userId: String) {
                    if (selectedIds.contains(userId)) {
                        selectedIds.remove(userId)
                    } else {
                        selectedIds.add(userId)
                    }
                }
            })
            addRecyclerView.adapter = adapter
            addRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        return view
    }

    private var groupImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            groupImageView.setImageURI(imageUri)
            groupImageUri = imageUri
        }
    }
}

interface OnUserClickListener {
    fun onUserClick(userId: String)
    fun isSelected(userId: String): Boolean {
        return false
    }
}

class AddMemberAdapter(

    private val userList: List<Users>,
    private val listener: OnUserClickListener
) : RecyclerView.Adapter<AddMemberAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friends_checkbox, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
        holder.itemView.setOnClickListener {
            listener.onUserClick(user.userID)
            holder.friendCheckBox.isChecked = holder.friendCheckBox.isChecked.not()
        }
        holder.friendCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listener.onUserClick(user.userID)
            } else {
                listener.onUserClick(user.userID)
            }
        }

        holder.friendCheckBox.isChecked = listener.isSelected(user.userID)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendImage: ImageView = itemView.findViewById(R.id.friend_image)
        val friendName: TextView = itemView.findViewById(R.id.friend_name)
        val friendId: TextView = itemView.findViewById(R.id.friend_id)
        val friendCheckBox: CheckBox = itemView.findViewById(R.id.friend_checkbox)

        fun bind(user: Users) {
            friendName.text = user.username
            friendId.text = user.userID
            Glide.with(friendImage)
                .load(user.imageUrl)
                .placeholder(R.drawable.default_avatar)
                .into(friendImage)
        }
    }
}
object FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getUsers(onResult: (List<Users>) -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("Users")
            .whereNotEqualTo("uid", currentUserUid)
            .get()
            .addOnSuccessListener { result ->
                val userList = mutableListOf<Users>()
                for (document in result) {
                    val user = document.toObject(Users::class.java)
                    userList.add(user)
                }
                onResult(userList)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting users", exception)
                onResult(emptyList())
            }
    }

    fun createGroup(group: Group, onResult: (String?) -> Unit) {
        firestore.collection("groups")
            .add(group)
            .addOnSuccessListener { documentReference ->
                onResult(documentReference.id)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error creating group", exception)
                onResult(null)
            }
    }
}