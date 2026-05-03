package com.example.todo.screens.dashboard

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.example.todo.R
import com.example.todo.adapter.TaskAdapter
import com.example.todo.data.models.TaskItem

class TrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        findViewById<ImageView>(R.id.imageviewBackTrash).setOnClickListener { finish() }

        val listView = findViewById<ListView>(R.id.listViewTrash)
        val trashList = ArrayList<TaskItem>()
        listView.adapter = TaskAdapter(this, trashList)
    }
}