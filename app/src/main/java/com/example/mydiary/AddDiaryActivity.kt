package com.example.mydiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mydiary.databinding.LayoutMiscellaneousBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private lateinit var selectedDiaryColor:String
    private lateinit var viewSubtitleIndicator: View
    private lateinit var imageColor1: ImageView
    private lateinit var imageColor2: ImageView
    private lateinit var imageColor3: ImageView
    private lateinit var imageColor4: ImageView
    private lateinit var imageColor5: ImageView
    private lateinit var layoutAddImage: LinearLayout

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
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)
        imageColor1= findViewById(R.id.imageColor1)
        imageColor2= findViewById(R.id.imageColor2)
        imageColor3= findViewById(R.id.imageColor3)
        imageColor4= findViewById(R.id.imageColor4)
        imageColor5= findViewById(R.id.imageColor5)
        layoutAddImage = findViewById(R.id.layoutAddImage)

        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        selectedDiaryColor = "#333333"

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

        imageColor1.setOnClickListener(){
            selectedDiaryColor = "#333333"
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor2.setOnClickListener(){
            selectedDiaryColor = "#FDBE3B"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor3.setOnClickListener(){
            selectedDiaryColor = "#FF4842"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor4.setOnClickListener(){
            selectedDiaryColor = "#3A52FC"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor5.setOnClickListener(){
            selectedDiaryColor = "#000000"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }

        layoutAddImage.setOnClickListener(){

        }

        initMiscellaneous()
        setSubtitleIndicatorColor()
    }

    private fun initMiscellaneous() {
        val layoutMiscellaneous = findViewById<LinearLayout>(R.id.layoutMiscellaneous)
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous)

        layoutMiscellaneous.findViewById<View>(R.id.textMiscellaneous).setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun setSubtitleIndicatorColor() {
        val gradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedDiaryColor))
    }


    private fun addDiaryEntry() {
        val title = diaryTitle.text.toString().trim()
        val date = diaryDate.text.toString().trim()
        val time = diaryTime.text.toString().trim()
        val description = diaryDescription.text.toString().trim()
        val diaryColor = viewSubtitleIndicator.background.toString().trim()

        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = mAuth.currentUser?.uid ?: return

        val diaryEntry = Diary(title, date, time, description,diaryColor)
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