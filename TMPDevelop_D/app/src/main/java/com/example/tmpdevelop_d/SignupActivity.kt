package com.example.tmpdevelop_d

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.tmpdevelop_d.Users.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SignupActivity : AppCompatActivity() {
    // 宣告 Firebase Authentication 和 Firebase Firestore 物件
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // 宣告 TAG，用於在 Log 中區分不同訊息
    private val TAG = "SignupActivity"


    // 產生唯一的 User ID
    private fun generateUserID(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val userID = StringBuilder()
        for (i in 0 until 6) {
            userID.append(chars[random.nextInt(chars.length)])
        }
        return userID.toString()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 初始化 Firebase Authentication 和 Firebase Firestore 物件
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val signupButton = findViewById<Button>(R.id.btn_signup2)
        val emailText = findViewById<TextView>(R.id.txtEM)
        val passwordText = findViewById<TextView>(R.id.txtPW)
        val username = findViewById<TextView>(R.id.name)

        // 設定 "註冊" 按鈕的點擊監聽器
        signupButton.setOnClickListener {
            // 讀取使用者輸入的註冊資訊
            val email = emailText.text.toString()
            val username = username.text.toString()
            val password = passwordText.text.toString()
            val imageUrl = "https://example.com/default.jpg" // 用戶頭像的預設 URL

            // 確認使用者輸入的資訊不為空
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // 使用 Firebase Authentication API 建立新的帳號
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // 註冊成功，取得目前的使用者帳號
                            val user = auth.currentUser

                            // 取得 Firebase Auth 提供的 UID
                            val uid = user?.uid

                            // 產生唯一的 User ID
                            val userID = generateUserID()

                            // 將新用戶存儲到 Firebase Firestore
                            val newUser = Users(userID, username, imageUrl, uid)
                            firestore.collection("Users").document(userID)
                                .set(newUser)
                                .addOnSuccessListener {
                                    // 更新使用者帳號的顯示名稱
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build()
                                    user?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // 顯示名稱更新成功
                                                Log.d(TAG, "User profile updated.")
                                                // 顯示提示訊息給使用者
                                                Toast.makeText(
                                                    this@SignupActivity,
                                                    "註冊成功！",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                                // 重新導向回登入頁面
                                                val intent =
                                                    Intent(
                                                        this@SignupActivity,
                                                        LoginActivity::class.java
                                                    )
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                // 顯示名稱更新失敗，顯示錯誤訊息
                                                Log.d(
                                                    TAG,
                                                    "Failed to update user profile.",
                                                    task.exception
                                                )
                                                Toast.makeText(
                                                    this@SignupActivity,
                                                    "註冊失敗，請稍後再試。",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                        }
                    }
            }
        }
    }
}