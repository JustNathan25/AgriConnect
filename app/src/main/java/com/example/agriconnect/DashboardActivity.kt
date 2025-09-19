package com.example.agriconnect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Load Home fragment (if you have one)
                    true
                }
                R.id.nav_liked -> {
                    // Open Liked fragment or activity
                    true
                }
                R.id.nav_explore -> {
                    // Open Explore fragment or activity
                    true
                }
                R.id.nav_chat -> {
                    // 👉 Open MessagingActivity when Chat is clicked
                    startActivity(Intent(this, MessagingActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // Open Profile fragment or activity
                    true
                }
                else -> false
            }
        }
    }
}
