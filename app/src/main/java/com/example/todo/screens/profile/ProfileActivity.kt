package com.example.todo.screens.profile



import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.todo.R
import com.example.todo.screens.login.LoginActivity



import androidx.appcompat.app.AppCompatActivity
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val textviewUsernameHeader = findViewById<TextView>(R.id.textviewUsernameHeader)
        val textviewEmail = findViewById<TextView>(R.id.textviewEmail)
        val imageviewBack = findViewById<ImageView>(R.id.imageviewBack)
        val textviewLogoutButton = findViewById<TextView>(R.id.textviewLogoutButton)

        // Load data from memory
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        textviewUsernameHeader.text = sharedPref.getString("username", "N/A")
        textviewEmail.text = sharedPref.getString("email", "N/A")

        imageviewBack.setOnClickListener {
            finish()
        }
        val ivBack = findViewById<ImageView>(R.id.ivBack)

        ivBack.setOnClickListener {
            finish()
        }

        textviewLogoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}