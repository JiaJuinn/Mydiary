package com.example.mydiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var diaryTitle: EditText
    private lateinit var diaryDate: EditText
    private lateinit var diaryTime: EditText
    private lateinit var diaryDescription: EditText
    private lateinit var addDiaryButton: Button
    private lateinit var backBtn: ImageButton
    private lateinit var diaryImage: ImageView
    private lateinit var selectedImageView: ImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diary)

        diaryTitle = findViewById(R.id.diaryTitle)
        diaryDate = findViewById(R.id.diaryDate)
        diaryTime = findViewById(R.id.diaryTime)
        diaryDescription = findViewById(R.id.diaryDescription)
        addDiaryButton = findViewById(R.id.addDiaryButton)
        backBtn = findViewById(R.id.backBtn)
        diaryImage = findViewById(R.id.diaryImage)

        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference

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

        diaryImage.setOnClickListener { openFileChooser() }

        addDiaryButton.setOnClickListener { addDiaryEntry() }

        backBtn.setOnClickListener { finish() }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            // Create a new ImageView instance to load the selected image
            selectedImageView = ImageView(this)
            selectedImageView.setImageURI(data.data)
            diaryImage.setImageDrawable(selectedImageView.drawable)
        }
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

        val drawable = diaryImage.drawable ?: getDrawable(R.drawable.default_image)
        if (drawable == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> {
                // Convert other drawable types to Bitmap
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }

        // Compress the bitmap to bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Generate a unique ID for the image
        val imageId = UUID.randomUUID().toString()
        val imageRef = storageRef.child("diary_images/$userId/$imageId")

        // Upload the image
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val diaryEntry = Diary(title, date, time, description, imageUrl)
                    databaseRef.child("users").child(userId).child("diaryEntries").push().setValue(diaryEntry)
                        .addOnCompleteListener { entryTask ->
                            if (entryTask.isSuccessful) {
                                Toast.makeText(this, "Diary entry added", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

