package com.example.todo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.todo.R
import com.example.todo.data.models.TaskItem

class TaskAdapter(private val context: Context, private val taskList: ArrayList<TaskItem>) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = taskList.size
    override fun getItem(position: Int): Any = taskList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.item_task, parent, false)
        val currentTask = taskList[position]

        // Ensure you have these IDs in a small layout file named item_task.xml
        view.findViewById<TextView>(R.id.textviewTaskTitle).text = currentTask.title
        view.findViewById<ImageView>(R.id.imageviewTaskIcon).setImageResource(currentTask.iconRes)

        return view
    }
}