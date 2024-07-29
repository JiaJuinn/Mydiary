package com.example.mydiary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var fab: ImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var userId: String
    private lateinit var adapter: DiaryAdapter
    private lateinit var signOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        diaryRecyclerView = findViewById(R.id.diaryRecyclerView)
        fab = findViewById(R.id.fab)
        signOut = findViewById(R.id.signOutBtn)
        mAuth = FirebaseAuth.getInstance()

        // Get the current user's ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userId = currentUser.uid
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val options = FirebaseRecyclerOptions.Builder<Diary>()
            .setQuery(
                FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("diaryEntries"), Diary::class.java
            )
            .build()

        adapter = DiaryAdapter(options)
        diaryRecyclerView.adapter = adapter
        diaryRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : DiaryAdapter.OnItemClickListener {
            override fun onItemClick(diary: Diary, position: Int) {
                val diaryId = adapter.snapshots.getSnapshot(position).key

                if (diaryId != null) {
                    val intent = Intent(this@MainActivity, diaryDetails::class.java)
                    intent.putExtra("diaryId", diaryId)
                    startActivity(intent)
                } else {
                    // Handle case when diaryId is null
                    Toast.makeText(this@MainActivity, "Invalid diary entry", Toast.LENGTH_SHORT).show()
                }
            }
        })

        fab.setOnClickListener {
            val intent = Intent(this, AddDiaryActivity::class.java)
            startActivity(intent)
        }

//        Prevent user to multiple backing operation
        signOut.setOnClickListener {
            signOutUser()
            val intent = Intent(this, enterPage::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish() // Optionally finish the current activity
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun signOutUser() {
        mAuth.signOut()
        Toast.makeText(this, "User signed out.", Toast.LENGTH_SHORT).show()
    }
}
