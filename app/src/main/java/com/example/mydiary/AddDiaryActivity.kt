package com.example.mydiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var diaryTitle: EditText
    private lateinit var diaryDate: EditText
    private lateinit var diaryTime: EditText
    private lateinit var diaryDescription: EditText
    private lateinit var addDiaryButton: ImageView
    private lateinit var backBtn : ImageView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diary)

        diaryTitle = findViewById(R.id.diaryTitle)
        diaryDate = findViewById(R.id.diaryDate)
        diaryTime = findViewById(R.id.diaryTime)
        diaryDescription = findViewById(R.id.diaryDescription)
        addDiaryButton = findViewById(R.id.addDiaryButton)
        backBtn = findViewById(R.id.imageBack)

        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        val calendar = Calendar.getInstance()
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            diaryDate.setText("$day/${month + 1}/$year")
        }

        diaryDate.setOnClickListener {
            DatePickerDialog(this, dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            diaryTime.setText("$hour:$minute")
        }

        diaryTime.setOnClickListener {
            TimePickerDialog(this, timeListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        addDiaryButton.setOnClickListener { addDiaryEntry() }

        backBtn.setOnClickListener { finish() }
    }

    private fun addDiaryEntry() {
        val title = diaryTitle.text.toString().trim()
        val date = diaryDate.text.toString().trim()
        val time = diaryTime.text.toString().trim()
        val description = diaryDescription.text.toString().trim()

        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = mAuth.currentUser?.uid ?: return

        val diaryEntry = Diary(title, date, time, description)
        databaseRef.child("users").child(userId).child("diaryEntries").push().setValue(diaryEntry)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Diary entry added", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
                }
            }
    }
}