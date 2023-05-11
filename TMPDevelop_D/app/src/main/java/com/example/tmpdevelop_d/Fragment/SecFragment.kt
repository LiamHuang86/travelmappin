package com.example.tmpdevelop_d.Fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmpdevelop_d.Adapter.RecyclerViewAdapter
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SecFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter
    private val userList = arrayListOf<Users>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sec, container, false)
        recyclerView = view.findViewById(R.id.recycle_friends)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = RecyclerViewAdapter(userList)
        recyclerView.adapter = adapter

        // Access a Cloud Firestore instance from your Activity
        val db = Firebase.firestore

        // Get the collection reference
        val usersRef = db.collection("Users")

        // Retrieve data from Firestore and populate ArrayList
        usersRef.get().addOnSuccessListener { result ->
            userList.clear()
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            for (document in result) {
                val user = document.toObject(Users::class.java)
                if (user.uid != currentUserUid) {
                    userList.add(user)
                }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
        }

        return view
    }
}