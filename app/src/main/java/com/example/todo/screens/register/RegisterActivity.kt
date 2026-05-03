package com.example.todo.screens.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.todo.R


import android.content.Context

import android.widget.*
import com.example.todo.screens.login.LoginActivity

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val etRegEmail = findViewById<EditText>(R.id.etRegEmail)
        val etRegUsername = findViewById<EditText>(R.id.etRegUsername)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        btnSignUp.setOnClickListener {
            val email = etRegEmail.text.toString().trim()
            val username = etRegUsername.text.toString().trim()
            val password = etRegPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()

            // Validation: Check if all fields are filled
            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirm.isNotEmpty()) {
                // Validation: Check if passwords match
                if (password == confirm) {
                    val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("email", email)
                    editor.putString("username", username)
                    editor.putString("password", password)
                    editor.apply()

                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}