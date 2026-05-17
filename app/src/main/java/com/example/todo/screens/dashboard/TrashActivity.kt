package com.example.todo.screens.dashboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.todo.R
import com.example.todo.data.TaskRepository
import com.example.todo.data.models.TaskItem

class TrashActivity : Activity() {

    private lateinit var listView: ListView
    private lateinit var adapter: TrashAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        listView = findViewById(R.id.listViewTrash)

        findViewById<ImageView>(R.id.imageviewBackTrash).setOnClickListener { finish() }

        adapter = TrashAdapter()
        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    private fun refreshList() {
        adapter.notifyDataSetChanged()
    }

    inner class TrashAdapter : BaseAdapter() {

        override fun getCount(): Int = TaskRepository.trashTasks.size
        override fun getItem(pos: Int): TaskItem = TaskRepository.trashTasks[pos]
        override fun getItemId(pos: Int): Long = getItem(pos).id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val task = getItem(position)

            val row = LinearLayout(this@TrashActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 24, 0, 24)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val titleTv = TextView(this@TrashActivity).apply {
                text = task.title
                textSize = 15f
                setTextColor(0xFF94A3B8.toInt())
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val restoreBtn = TextView(this@TrashActivity).apply {
                text = "Restore"
                textSize = 13f
                setTextColor(0xFF1E293B.toInt())
                setPadding(16, 0, 16, 0)
            }

            val deleteBtn = TextView(this@TrashActivity).apply {
                text = "Delete"
                textSize = 13f
                setTextColor(0xFFEF4444.toInt())
                setPadding(0, 0, 0, 0)
            }

            restoreBtn.setOnClickListener {
                TaskRepository.restoreFromTrash(task)
                refreshList()
                Toast.makeText(this@TrashActivity, "\"${task.title}\" restored", Toast.LENGTH_SHORT).show()
            }

            deleteBtn.setOnClickListener {
                AlertDialog.Builder(this@TrashActivity)
                    .setTitle("Delete Permanently")
                    .setMessage("Permanently delete \"${task.title}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        TaskRepository.trashTasks.removeAll { it.id == task.id }
                        // Also remove from projects map if present
                        task.projectName?.let { pname ->
                            TaskRepository.projects[pname]?.removeAll { it.id == task.id }
                        }
                        refreshList()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            row.addView(titleTv)
            row.addView(restoreBtn)
            row.addView(deleteBtn)

            // long press = restore shortcut
            row.setOnLongClickListener {
                TaskRepository.restoreFromTrash(task)
                refreshList()
                Toast.makeText(this@TrashActivity, "\"${task.title}\" restored", Toast.LENGTH_SHORT).show()
                true
            }

            return row
        }
    }
}