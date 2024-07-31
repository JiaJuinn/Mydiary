package com.example.mydiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.UUID
import kotlin.math.log

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var diaryTitle: EditText
    private lateinit var diarySubtitle: EditText
    private lateinit var diaryDate: EditText
    private lateinit var diaryTime: EditText
    private lateinit var diaryImage: ImageView
    private lateinit var diaryDescription: EditText
    private lateinit var addDiaryButton: ImageView
    private lateinit var backBtn: ImageView
    private lateinit var selectedDiaryColor: String
    private lateinit var viewSubtitleIndicator: View
    private lateinit var imageColor1: ImageView
    private lateinit var imageColor2: ImageView
    private lateinit var imageColor3: ImageView
    private lateinit var imageColor4: ImageView
    private lateinit var imageColor5: ImageView
    private lateinit var layoutAddImage: LinearLayout


    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    private var filePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diary)

        diaryTitle = findViewById(R.id.diaryTitle)
        diarySubtitle = findViewById(R.id.inputDiarySubtitle)
        diaryDate = findViewById(R.id.diaryDate)
        diaryTime = findViewById(R.id.diaryTime)
        diaryImage = findViewById(R.id.imageDiary)
        diaryDescription = findViewById(R.id.diaryDescription)
        addDiaryButton = findViewById(R.id.addDiaryButton)
        backBtn = findViewById(R.id.imageBack)
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)
        imageColor1 = findViewById(R.id.imageColor1)
        imageColor2 = findViewById(R.id.imageColor2)
        imageColor3 = findViewById(R.id.imageColor3)
        imageColor4 = findViewById(R.id.imageColor4)
        imageColor5 = findViewById(R.id.imageColor5)
        layoutAddImage = findViewById(R.id.layoutAddImage)

        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference

        selectedDiaryColor = "#333333" //default color


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

        imageColor1.setOnClickListener {
            selectedDiaryColor = "#333333"
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor2.setOnClickListener {
            selectedDiaryColor = "#FDBE3B"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor3.setOnClickListener {
            selectedDiaryColor = "#FF4842"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor4.setOnClickListener {
            selectedDiaryColor = "#3A52FC"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        imageColor5.setOnClickListener {
            selectedDiaryColor = "#000000"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }

        layoutAddImage.setOnClickListener {
            openFileChooser()
        }

        initMiscellaneous()
        setSubtitleIndicatorColor()
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val REQUEST_CODE_SELECT_IMAGE = 1
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let { selectedImageUri ->
                try {
                    val inputStream = contentResolver.openInputStream(selectedImageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    diaryImage.setImageBitmap(bitmap)
                    diaryImage.visibility = View.VISIBLE
                } catch (exception: Exception) {
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        val subtitle = diarySubtitle.text.toString().trim()
        val date = diaryDate.text.toString().trim()
        val time = diaryTime.text.toString().trim()
        val description = diaryDescription.text.toString().trim()
        val diaryColor = selectedDiaryColor

        if (title.isEmpty()) {
            Toast.makeText(this, "Please fill out title fields", Toast.LENGTH_SHORT).show()
            return
        }

        val drawable = diaryImage.drawable ?: getDrawable(R.drawable.default_image)
        if (drawable == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = mAuth.currentUser?.uid ?: return

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
        val imagePath = storageRef.child("diary_images/$userId/$imageId")

        if (imagePath != null) {
            val uploadTask = imagePath.putBytes(data)
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imagePath.downloadUrl.addOnCompleteListener { uploadImage ->
                        val imageURL = uploadImage.toString()
                        val diaryEntry =
                            Diary(title, subtitle, date, time, description, diaryColor, imageURL)
                        databaseRef.child("users").child(userId).child("diaryEntries").push()
                            .setValue(diaryEntry)
                            .addOnCompleteListener { task2 ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Diary entry added", Toast.LENGTH_SHORT)
                                        .show()
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to add diary entry",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {

            val diaryEntry =
                Diary(title, subtitle, date, time, description, diaryColor, diaryImage.toString())

            databaseRef.child("users").child(userId).child("diaryEntries").push()
                .setValue(diaryEntry)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Diary entry added", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
                    }
                }
        }


//        if (filePath != null) {
//            val ref = storageRef.child("images/$userId/${System.currentTimeMillis()}.jpg")
//            ref.putFile(filePath!!).addOnSuccessListener {
//                ref.downloadUrl.addOnSuccessListener { uri ->
//                    val diaryEntry = Diary(title, subtitle, date, time, description, diaryColor, uri.toString())
//                    databaseRef.child("users").child(userId).child("diaryEntries").push().setValue(diaryEntry)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                Toast.makeText(this, "Diary entry added", Toast.LENGTH_SHORT).show()
//                                finish()
//                            } else {
//                                Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                }
//            }.addOnFailureListener { e ->
//                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            val diaryEntry = Diary(title, subtitle, date, time, description, diaryColor, diaryImage.toString())
//            databaseRef.child("users").child(userId).child("diaryEntries").push().setValue(diaryEntry)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(this, "Diary entry added", Toast.LENGTH_SHORT).show()
//                        finish()
//                    } else {
//                        Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
    }
}