package com.example.tmpdevelop_d

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var messageTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailEditText = findViewById(R.id.txtEM)    //回傳註冊的信箱
        messageTextView = findViewById(R.id.txtT2)  //重設密碼回傳提示訊息

        val resetButton = findViewById<Button>(R.id.btn_reset)
        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "必須輸入正確的註冊信箱"
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        messageTextView.text = "密碼已成功發送至 $email"

                    } else {
                        messageTextView.text = "發送失敗"
                    }
                }
        }
    }
}