package com.example.agriconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agriconnect.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // SIGN IN button
        binding.button.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.passET.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            // 🔹 Check if user has displayName in Firestore
                            firestore.collection("users").document(user.uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val displayName = document.getString("displayName")
                                        if (!displayName.isNullOrEmpty()) {
                                            // ✅ Already has a name → go to dashboard
                                            startActivity(Intent(this, DashboardActivity::class.java))
                                        } else {
                                            // 🆕 No name yet → go to SetDisplayNameActivity
                                            startActivity(Intent(this, SetDisplayNameActivity::class.java))
                                        }
                                    } else {
                                        // 🔹 No user doc found → force setup
                                        startActivity(Intent(this, SetDisplayNameActivity::class.java))
                                    }
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("SignInActivity", "Error fetching user profile", e)
                                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                                    // fallback: dashboard
                                    startActivity(Intent(this, DashboardActivity::class.java))
                                    finish()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // "Sign Up" text → go to SignUpActivity
        binding.signup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
