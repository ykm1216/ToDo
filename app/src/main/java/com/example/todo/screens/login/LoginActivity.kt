package com.example.todo.screens.login

import android.os.Bundle
import com.example.todo.R



import android.app.Activity
import android.content.Intent

import android.widget.*
import com.example.todo.screens.dashboard.DashboardActivity
import com.example.todo.screens.register.RegisterActivity

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val etUsername = findViewById<EditText>(R.id.edittextUsername)
        val etPassword = findViewById<EditText>(R.id.edittextPassword)
        val textviewRegister = findViewById<TextView>(R.id.textviewRegister)

        // AUTO-FILL LOGIC: Retrieve saved data immediately
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedUser = sharedPref.getString("username", "")
        val savedPass = sharedPref.getString("password", "")

        if (!savedUser.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
            etUsername.setText(savedUser)
            etPassword.setText(savedPass)
        }

        btnSignIn.setOnClickListener {
            val inputUser = etUsername.text.toString().trim()
            val inputPass = etPassword.text.toString().trim()

            // Re-read here so we always get the latest registered credentials
            val currentUser = sharedPref.getString("username", "")
            val currentPass = sharedPref.getString("password", "")

            if (inputUser.isNotEmpty() && inputPass.isNotEmpty()) {
                if (inputUser == currentUser && inputPass == currentPass) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        textviewRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}