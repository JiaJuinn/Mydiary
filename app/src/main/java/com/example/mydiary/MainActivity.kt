package com.example.mydiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var fab: ImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var adapter: DiaryAdapter
    private lateinit var signOut: Button
    private lateinit var searchDiary: SearchView
    private var itemList: ArrayList<Diary> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        diaryRecyclerView = findViewById(R.id.diaryRecyclerView)
        fab = findViewById(R.id.fab)
        signOut = findViewById(R.id.signOutBtn)
        searchDiary = findViewById(R.id.inputSearch)
        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(userId)
            .child("diaryEntries")

        val options = FirebaseRecyclerOptions.Builder<Diary>()
            .setQuery(databaseRef, Diary::class.java)
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
                    Toast.makeText(this@MainActivity, "Invalid diary entry", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Populate itemList with all diary entries from Firebase
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Diary::class.java)
                    if (item != null) {
                        itemList.add(item)
                    }
                }
                Log.d("MainActivity", "itemList size: ${itemList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to retrieve items", Toast.LENGTH_SHORT).show()
            }
        })

        searchDiary.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                Log.d("MainActivity", "Query submitted: $query")
                val filteredList = ArrayList<Diary>()
                for (diary in itemList) {
                    if (diary.title?.lowercase()?.contains(query?.lowercase() ?: "") == true) {
                        filteredList.add(diary)
                    }
                }
                Log.d("MainActivity", "Filtered list size: ${filteredList.size}")
                adapter.searchItemList(filteredList)
                return true
            }
        })

        fab.setOnClickListener {
            val intent = Intent(this, AddDiaryActivity::class.java)
            startActivity(intent)
        }

        signOut.setOnClickListener {
            signOutUser()
            val intent = Intent(this, enterPage::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
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


