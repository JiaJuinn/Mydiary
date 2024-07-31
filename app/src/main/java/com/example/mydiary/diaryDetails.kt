package com.example.mydiary

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class diaryDetails : AppCompatActivity() {

    private lateinit var backBtn: ImageView

    private lateinit var diaryTitle: EditText
    private lateinit var diarySubTitle: EditText
    private lateinit var diaryImage: ImageView
    private lateinit var diaryDate: EditText
    private lateinit var diaryTime: EditText
    private lateinit var diaryDescription: EditText

    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_details)

        diaryTitle = findViewById(R.id.diaryTitle)
        diarySubTitle = findViewById(R.id.inputDiarySubtitle)
        diaryImage  = findViewById(R.id.imageDiary)
        diaryDate = findViewById(R.id.diaryDate)
        diaryTime = findViewById(R.id.diaryTime)
        diaryDescription = findViewById(R.id.diaryDescription)
        diaryImage = findViewById(R.id.imageDiary)
        backBtn = findViewById(R.id.imageBack)

        // Initialize Firebase Database
        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        val diaryId = intent.getStringExtra("diaryId")
        Log.d("diaryId", diaryId ?: "diaryId is null")

        backBtn.setOnClickListener { finish() }

        val userId = mAuth.currentUser?.uid ?: return

        // Fetch data from Firebase
        databaseRef.child("users").child(userId).child("diaryEntries").child(diaryId ?: "")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val diaryTitle = dataSnapshot.child("title").getValue(String::class.java)
                        val diarySubTitle = dataSnapshot.child("subtitle").getValue(String::class.java)
                        val diaryImageUrl = dataSnapshot.child("diaryImage").getValue(String::class.java)
                        val diaryDate = dataSnapshot.child("date").getValue(String::class.java)
                        val diaryTime = dataSnapshot.child("time").getValue(String::class.java)
                        val diaryDescription = dataSnapshot.child("description").getValue(String::class.java)

                        // Update the UI with the fetched data
                        this@diaryDetails.diaryTitle.setText(diaryTitle)
                        this@diaryDetails.diarySubTitle.setText(diarySubTitle)
                        this@diaryDetails.diaryDate.setText(diaryDate)
                        this@diaryDetails.diaryTime.setText(diaryTime)
                        this@diaryDetails.diaryDescription.setText(diaryDescription)

                        Glide.with(applicationContext)
                            .load(diaryImageUrl)
                            .placeholder(R.drawable.default_image) // Replace with your placeholder image
                            .error(R.drawable.default_image) // Replace with your error image
                            .into(diaryImage)

                        Log.d("FirebaseData", "Diary ID: $diaryId, Title: $diaryTitle, Description: $diaryDescription")
                        Log.d("ImageUri", "Selected Image URI: $diaryImageUrl")

                    } else {
                        Log.d("FirebaseData", "No data found")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseData", "Error fetching data", databaseError.toException())
                }
            })
    }
}
