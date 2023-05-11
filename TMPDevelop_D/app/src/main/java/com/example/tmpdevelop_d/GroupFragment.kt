package com.example.tmpdevelop_d

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.viewpager.widget.ViewPager
import com.example.tmpdevelop_d.Adapter.ViewPagerAdapter
import com.example.tmpdevelop_d.databinding.FragmentGroupBinding
import com.google.android.material.tabs.TabLayout


class GroupFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var imgBtn: ImageButton

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

        //val navController = findNavController()
        //NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController)

        // 設置Toolbar標題
        //NavigationUI.setupWithNavController(binding.toolbar, navController)
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
}