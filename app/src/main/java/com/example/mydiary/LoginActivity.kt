package com.example.mydiary

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var registerLink: TextView
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var backBtn: ImageButton
    private lateinit var loginButton: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views and FirebaseAuth
        signupEmail = findViewById(R.id.signupEmail)
        signupPassword = findViewById(R.id.signupPassword)
        loginButton = findViewById(R.id.loginBtn)
        registerLink = findViewById(R.id.registerLink)
        backBtn = findViewById(R.id.backBtn)

        mAuth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (mAuth.currentUser != null) {
            // User is already logged in, redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Set up click listeners
        loginButton.setOnClickListener { loginUser() }
        registerLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loginUser() {
        val email = signupEmail.text.toString().trim()
        val password = signupPassword.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            signupEmail.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(password)) {
            signupPassword.error = "Password is required."
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
