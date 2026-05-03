package com.example.todo.screens.dashboard

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.example.todo.R
import com.example.todo.adapter.TaskAdapter
import com.example.todo.data.models.TaskItem

class SectionListActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_list)

        val title = intent.getStringExtra("section_title") ?: "Tasks"

        findViewById<TextView>(R.id.textviewSectionTitle).text = title
        findViewById<ImageView>(R.id.imageviewBackSection).setOnClickListener { finish() }

        // Empty list for now — wire up real data later
        val listView = findViewById<ListView>(R.id.listViewSectionTasks)
        val taskList = ArrayList<TaskItem>()
        listView.adapter = TaskAdapter(this, taskList)
    }
}