package com.example.todo.screens.profile

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.todo.R
import com.example.todo.screens.calendar.CalendarActivity
import com.example.todo.screens.dashboard.DashboardActivity
import com.example.todo.screens.login.LoginActivity
import com.example.todo.screens.projects.ProjectsActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        findViewById<TextView>(R.id.textviewUsernameHeader).text =
            sharedPref.getString("username", "N/A")
        findViewById<TextView>(R.id.textviewEmail).text =
            sharedPref.getString("email", "N/A")

        // Logout
        findViewById<TextView>(R.id.textviewLogoutButton).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Bottom nav
        findViewById<LinearLayout>(R.id.linearlayoutNavTodo).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
        findViewById<LinearLayout>(R.id.linearlayoutNavProjects).setOnClickListener {
            startActivity(Intent(this, ProjectsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
        findViewById<LinearLayout>(R.id.linearlayoutNavCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
    }
}