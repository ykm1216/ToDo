package com.example.todo.screens.calendar

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.example.todo.R
import com.example.todo.screens.profile.ProfileActivity

class CalendarActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val navTodo = findViewById<LinearLayout>(R.id.linearlayoutNavTodo)
        val navCalendar = findViewById<LinearLayout>(R.id.linearlayoutNavCalendar)
        val navProfile = findViewById<LinearLayout>(R.id.linearlayoutNavProfile)
        val addTaskBtn = findViewById<ImageButton>(R.id.imagebuttonAddTaskCalendar)


        navTodo.setOnClickListener {
            val intent = Intent(this, com.example.todo.screens.dashboard.DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
           navCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        addTaskBtn.setOnClickListener {
            val optionsView = layoutInflater.inflate(R.layout.dialog_add_task_options, null)
            val optionsDialog = AlertDialog.Builder(this)
                .setView(optionsView)
                .create()

            optionsView.findViewById<LinearLayout>(R.id.optionNewTodo).setOnClickListener {
                optionsDialog.dismiss()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("New To-Do")
                val input = EditText(this)
                input.hint = "Task name"
                builder.setView(input)
                builder.setPositiveButton("Save") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) {
                        Toast.makeText(this, "Task added: $name", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("Cancel", null)
                builder.show()
            }

            optionsView.findViewById<LinearLayout>(R.id.optionNewProject).setOnClickListener {
                optionsDialog.dismiss()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("New Project")
                val input = EditText(this)
                input.hint = "Project name"
                builder.setView(input)
                builder.setPositiveButton("Create") { _, _ -> }
                builder.setNegativeButton("Cancel", null)
                builder.show()
            }

            optionsView.findViewById<LinearLayout>(R.id.optionNewArea).setOnClickListener {
                optionsDialog.dismiss()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("New Area")
                val input = EditText(this)
                input.hint = "Area name"
                builder.setView(input)
                builder.setPositiveButton("Create") { _, _ -> }
                builder.setNegativeButton("Cancel", null)
                builder.show()
            }

            optionsDialog.show()
        }


    }

}