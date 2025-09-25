package com.example.agriconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SetDisplayNameActivity : AppCompatActivity() {

    private lateinit var editDisplayName: EditText
    private lateinit var btnSaveName: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_display_name)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        editDisplayName = findViewById(R.id.etDisplayName)
        btnSaveName = findViewById(R.id.btnSaveName)

        btnSaveName.setOnClickListener {
            val name = editDisplayName.text.toString().trim()

            if (name.isNotEmpty()) {
                val user = auth.currentUser
                val uid = user?.uid

                if (user != null && uid != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    // 1️⃣ Update Firebase Authentication profile
                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("SetDisplayName", "Auth profile updated")

                                // 2️⃣ Save name to Firestore
                                val userDoc = mapOf(
                                    "displayName" to name,
                                    "email" to user.email
                                )

                                firestore.collection("users").document(uid)
                                    .set(userDoc)  // overwrite or create
                                    .addOnSuccessListener {
                                        Log.d("SetDisplayName", "Name saved to Firestore")
                                        Toast.makeText(this, "Welcome $name!", Toast.LENGTH_SHORT).show()

                                        // Go to Dashboard
                                        startActivity(Intent(this, DashboardActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("SetDisplayName", "Failed to save name", e)
                                        Toast.makeText(this, "Error saving name", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Log.e("SetDisplayName", "Failed to update profile", task.exception)
                                Toast.makeText(this, "Failed to set display name", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
