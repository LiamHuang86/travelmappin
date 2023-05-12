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
import com.google.firebase.storage.FirebaseStorage
import java.util.*

private const val DEFAULT_GROUP_IMAGE_URL = "https://your-default-image-url.com"

class CreateGroupDialog : DialogFragment(), OnUserClickListener {

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

        groupImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        finishButton.setOnClickListener {
            val groupName = groupNameEditText.text.toString()
            val groupID = generateUniqueGroupId()
            val creatorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (groupName.isNotEmpty() && creatorId.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE

                val memberIds = selectedIds.toMutableList()
                memberIds.add(creatorId) // Add the creator's ID to the list of members

                if (groupImageUri != null) {
                    val storageRef =
                        FirebaseStorage.getInstance().reference.child("group_images/${UUID.randomUUID()}")
                    storageRef.putFile(groupImageUri!!).addOnSuccessListener { uploadTask ->
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            saveGroupToFirestore(groupName, groupID, creatorId, downloadUri.toString(), memberIds)
                        }.addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(
                                context,
                                "Failed to get image download URL",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    saveGroupToFirestore(groupName, groupID, creatorId, DEFAULT_GROUP_IMAGE_URL, memberIds)
                }
            }
        }

        FirestoreRepository.getUsers { users ->
            userList = users
            val adapter = AddMemberAdapter(userList, this)
            addRecyclerView.adapter = adapter
            addRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        return view
    }

    private fun saveGroupToFirestore(groupName: String, groupID: String, creatorId: String, photoUrl: String, memberIds: MutableList<String>) {
        val totalMembers = memberIds.size
        val group = Group(id = "", groupId = groupID, groupName = groupName, creatorId = creatorId, photoUrl = photoUrl, memberIds = memberIds, totalMembers = totalMembers)


        val docRef = FirebaseFirestore.getInstance().collection("Groups").document(groupID)

        docRef.set(group)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                dismiss()
                Toast.makeText(context, "Group created successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to create group", Toast.LENGTH_SHORT).show()
            }
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

    override fun onUserClick(userID: String) {
        val user = userList.find { it.userID == userID }
        if (user != null) {
            val uid = user.uid
            if (uid != null) {
                if (selectedIds.contains(uid)) {
                    selectedIds.remove(uid)
                } else {
                    selectedIds.add(uid)
                }
            }
        }
    }

    override fun isSelected(userID: String): Boolean {
        val user = userList.find { it.userID == userID }
        return if (user != null) {
            val uid = user.uid
            selectedIds.contains(uid)
        } else {
            false
        }
    }

    private fun generateUniqueGroupId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val groupId = StringBuilder()

        for (i in 0 until 8) {
            groupId.append(chars[random.nextInt(chars.length)])
        }

        return groupId.toString()
    }
}

interface OnUserClickListener {
    fun onUserClick(uid: String)
    fun isSelected(uid: String): Boolean
}


class AddMemberAdapter(
    private val userList: List<Users>,
    private val listener: OnUserClickListener
) : RecyclerView.Adapter<AddMemberAdapter.ViewHolder>() {

    private val selectedUsers = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friends_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)

        holder.friendCheckBox.setOnCheckedChangeListener(null)
        holder.friendCheckBox.isChecked = isSelected(user.userID)

        holder.friendCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedUsers.add(user.userID)
            } else {
                selectedUsers.remove(user.userID)
            }
            listener.onUserClick(user.userID)
        }
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

    private fun isSelected(userId: String): Boolean {
        return selectedUsers.contains(userId)
    }
}

object FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getUsers(onResult: (List<Users>) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        firestore.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                val userList = mutableListOf<Users>()
                for (document in result) {
                    val user = document.toObject(Users::class.java)
                    if (user.uid != currentUserId) { // 只添加不是当前登录用户的其他用户
                        userList.add(user)
                    }
                }
                onResult(userList)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting users", exception)
                onResult(emptyList())
            }
    }
}