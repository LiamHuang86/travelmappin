package com.example.tmpdevelop_d

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AccountActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvUserID: TextView

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        imageView = findViewById(R.id.imageView2)
        tvUsername = findViewById(R.id.tV2)
        tvUserID = findViewById(R.id.tV3)

        // 登出按鈕
        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            logout()
        }

        // 更改頭像的點擊監聽器
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        // 讀取 Firestore 中的用戶資訊
        loadUserInfo()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            val imageUri = data?.data
            imageUri?.let {
                imageView.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val ref = storage.reference.child("images/${currentUser?.uid}")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrl(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Failed to upload image to cloud storage", e)
            }
    }

    private fun saveImageUrl(imageUrl: String) {
        currentUser?.let { user ->
            db.collection("Users").document(user.uid)
                .update("imageUrl", imageUrl)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                }
        }
    }

    // 讀取 Firestore 中的用戶資訊並將 ImageView 的圖片設置為 Uri 對應的圖片
    private fun loadUserInfo() {
        currentUser?.let { user ->
            db.collection("Users").whereEqualTo("uid", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val username = document.getString("username")
                        val userID = document.getString("userID")
                        val imageUrl = document.getString("imageUrl")
                        tvUsername.text = username
                        tvUserID.text = userID
                        // 如果 imageUrl 不為空，則使用 Glide 加載到 imageView
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .apply(RequestOptions.circleCropTransform())  //使用 Glide 的 circleCropTransform 方法來實現圓形圖片
                                .into(imageView)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting user info: ", exception)
                }
        }
    }

    companion object {
        private const val TAG = "AccountActivity"
    }
}