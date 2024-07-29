package com.example.mydiary

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class diaryDetails : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_details)

        backBtn = findViewById(R.id.imageBack)

        backBtn.setOnClickListener() {
            finish()
        }

    }
}