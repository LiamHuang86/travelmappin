package com.example.tmpdevelop_d

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.tmpdevelop_d.Adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var imgBtn: ImageButton

    // 初始化Firebase實例
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val REQUEST_CODE_ACCOUNT_ACTIVITY = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_group, container, false)

        viewPager = root.findViewById(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(childFragmentManager)

        tabLayout = root.findViewById(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)

        imgBtn = root.findViewById(R.id.img_btn)
        imgBtn.setOnClickListener {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ACCOUNT_ACTIVITY)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserImage()

        view.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                loadUserImage()
                return@OnKeyListener true
            }
            false
        })

        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ACCOUNT_ACTIVITY && resultCode == Activity.RESULT_OK) {
            // 获取选中图片的 Uri
            val imageUri = data?.data
            if (imageUri != null) {
                // 设置 ImageButton 的图片
                imgBtn.setImageURI(imageUri)
            }
        }
    }

    private fun loadUserImage() {
        currentUser?.let { user ->
            // 查詢uid與當前用戶uid匹配的文件
            db.collection("Users").whereEqualTo("uid", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // 獲取imageUrl字段的值
                        val imageUrl = document.getString("imageUrl")
                        // 如果 imageUrl 不為空，並且 getActivity 不為 null，則使用 Glide 加載到 imgBtn，並轉換為圓形
                        if (!imageUrl.isNullOrEmpty() && activity != null) {
                            Glide.with(this)
                                .load(imageUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .into(imgBtn)
                        }
                    }
                }
        }
    }
}