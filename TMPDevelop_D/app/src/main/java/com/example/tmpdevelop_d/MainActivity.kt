package com.example.tmpdevelop_d

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {



    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 改變默認標題 Action Bar 的背景顔色
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.main_color_05)))

        auth = Firebase.auth
        bottomNavigationView = findViewById(R.id.bottom_navigation)


        if (auth.currentUser == null) {
            // 未登入，跳轉至登入畫面
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // 結束 MainActivity，避免使用者按返回鍵回到此畫面
        } else {
            // 已登入，留在 MainActivity
            // TODO: 在此處顯示已登入用戶的內容
        }





        val mapFragment = MapFragment()
        val groupFragment = GroupFragment()
        val accountFragment = CostFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host, mapFragment)
            .commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_groups -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host, groupFragment).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.menu_map -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host, mapFragment).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.menu_costs -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host, accountFragment).commit()
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_group, R.id.navigation_map, R.id.navigation_cost)
            .build()

        bottomNavigationView.selectedItemId = R.id.menu_map // 設置預設選中的項目


    }
    fun setBottomNavigationVisibility(isVisible: Boolean) {
        if (isVisible) {
            bottomNavigationView.visibility = View.VISIBLE
        } else {
            bottomNavigationView.visibility = View.GONE
        }
    }
}
