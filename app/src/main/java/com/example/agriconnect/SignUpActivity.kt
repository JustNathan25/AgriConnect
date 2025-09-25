package com.example.agriconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.agriconnect.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Handle normal email signup
        binding.signupbutton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.passET.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SignUpActivity", "createUserWithEmail: success")
                        redirectUser()
                    } else {
                        Log.w("SignUpActivity", "createUserWithEmail: failure", task.exception)
                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Handle Google sign-in button click
        binding.imageView.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    // Register for Google sign-in result
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign in failed", e)
            Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("SignUpActivity", "signInWithCredential: success")
                    redirectUser()
                } else {
                    Log.w("SignUpActivity", "signInWithCredential: failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Redirect user after signup/signin.
     * Checks Firestore `users/{uid}` for displayName.
     */
    private fun redirectUser() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        val userDocRef = firestore.collection("users").document(uid)

        userDocRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && snapshot.getString("displayName")?.isNotEmpty() == true) {
                // ✅ Name already set → go to dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                // 🆕 First time: create/update doc with uid + email + empty displayName
                val newUser = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to "" // empty until SetDisplayNameActivity updates it
                )
                userDocRef.set(newUser)
                    .addOnSuccessListener {
                        startActivity(Intent(this, SetDisplayNameActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("SignUpActivity", "Failed to save user profile", e)
                        Toast.makeText(this, "Failed to save user profile", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java)) // fallback
                        finish()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("SignUpActivity", "userDocRef.get() failed", e)
            Toast.makeText(this, "Failed to check profile", Toast.LENGTH_SHORT).show()
            // fallback: go to dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
