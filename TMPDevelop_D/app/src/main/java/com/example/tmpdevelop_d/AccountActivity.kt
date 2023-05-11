package com.example.tmpdevelop_d

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvUserID: TextView
    private lateinit var tvChangePassword: TextView

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        imageView = findViewById(R.id.imageView2)
        tvUsername = findViewById(R.id.tV2)
        tvUserID = findViewById(R.id.tV3)

        // 設置登出按鈕的點擊監聽器
        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            logout()
        }

        // 設置更改頭像的點擊監聽器
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        // 從 Firestore 中讀取當前用戶的資訊，並顯示在相應的 TextView 上
        currentUser?.let { user ->
            db.collection("Users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        val userID = document.getString("userID")
                        tvUsername.text = username
                        tvUserID.text = userID
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting user info: ", exception)
                }
        }


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
                // 获取选中图片的 Uri
                val imageUri = data?.data
                if (imageUri != null) {
                    // 设置 ImageView 的图片
                    imageView.setImageURI(imageUri)
                }
            }
        }


        companion object {
            private const val TAG = "AccountActivity"
        }
    }
