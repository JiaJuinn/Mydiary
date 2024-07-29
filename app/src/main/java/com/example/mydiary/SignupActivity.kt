package com.example.mydiary

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class SignupActivity: AppCompatActivity() {

    private lateinit var loginLink: TextView
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerUsername: EditText
    private lateinit var registerConfirmPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var backBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPassword)
        registerConfirmPassword = findViewById(R.id.registerConfirmPassword)
        registerButton = findViewById(R.id.registerBtn)
        loginLink = findViewById(R.id.loginLink)
        backBtn = findViewById(R.id.imageBack)
        registerUsername = findViewById(R.id.userName)

        mAuth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener { registerUser() }
        loginLink.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        backBtn.setOnClickListener() {
            finish()
        }
    }

    private fun registerUser() {
        val email = registerEmail.text.toString().trim()
        val password = registerPassword.text.toString().trim()
        val confirmPassword = registerConfirmPassword.text.toString().trim()
        val userName = registerUsername.text.toString().trim()

        if (TextUtils.isEmpty(userName)) {
            registerUsername.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(email)) {
            registerEmail.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(password)) {
            registerPassword.error = "Password is required."
            return
        }

        if (password.length < 6) {
            registerPassword.error = "Password must be at least 6 characters."
            return
        }

        if (password != confirmPassword) {
            registerConfirmPassword.error = "Passwords do not match."
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val uid = user?.uid ?: ""
                    val fullName = userName
                    val email = email

                    val userDetails = userData(fullName, email)

                    // Store user details in Firebase Realtime Database
                    val database = FirebaseDatabase.getInstance()
                    val reference = database.getReference("users").child(uid).child("profile")


                    reference.setValue(userDetails).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "Registration successful.", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Failed to store user details: ${dbTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this,
                            "User with this email already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}
