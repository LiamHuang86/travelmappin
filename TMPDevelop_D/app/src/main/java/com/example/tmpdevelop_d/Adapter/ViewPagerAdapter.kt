package com.example.tmpdevelop_d.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tmpdevelop_d.Fragment.FirstFragment
import com.example.tmpdevelop_d.Fragment.SecFragment
import com.example.tmpdevelop_d.Fragment.ThirdFragment



class ViewPagerAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FirstFragment()
            1 -> SecFragment()
            2 -> ThirdFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "團隊"
            1 -> "好友"
            2 -> "行程"
            else -> null
        }
    }
}
