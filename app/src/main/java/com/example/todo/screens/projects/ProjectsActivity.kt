package com.example.todo.screens.projects

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import com.example.todo.R
import com.example.todo.data.TaskRepository
import com.example.todo.data.models.TaskItem
import com.example.todo.screens.calendar.CalendarActivity
import com.example.todo.screens.dashboard.DashboardActivity
import com.example.todo.screens.profile.ProfileActivity

class ProjectsActivity : Activity() {

    private lateinit var projectContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var emptyLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        projectContainer = findViewById(R.id.linearlayoutProjectContainer)
        searchInput      = findViewById(R.id.edittextSearch)
        emptyLabel       = findViewById(R.id.textviewEmptyProjects)

        setNavActive("projects")

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refreshList() }
        })

        // Add task button
        findViewById<ImageButton>(R.id.imagebuttonAddTask).setOnClickListener {
            showAddTaskOverlay()
        }

        // Nav
        findViewById<LinearLayout>(R.id.linearlayoutNavTodo).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
        findViewById<LinearLayout>(R.id.linearlayoutNavCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
        findViewById<LinearLayout>(R.id.linearlayoutNavProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        setNavActive("projects")
        refreshList()
    }

    // ── RENDER ────────────────────────────────────────────────────────────────

    private fun refreshList() {
        projectContainer.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()

        val filteredProjects = if (query.isEmpty()) {
            TaskRepository.projects.entries.toList()
        } else {
            TaskRepository.projects.entries.filter { (pname, tasks) ->
                pname.lowercase().contains(query) || tasks.any { it.title.lowercase().contains(query) }
            }
        }

        if (filteredProjects.isEmpty()) {
            emptyLabel.visibility = View.VISIBLE
            return
        }
        emptyLabel.visibility = View.GONE

        filteredProjects.forEach { (projectName, tasks) ->
            addProjectHeader(projectName)
            val filtered = if (query.isEmpty()) tasks
            else tasks.filter {
                it.title.lowercase().contains(query) || projectName.lowercase().contains(query)
            }
            filtered.forEach { addTaskRow(it, projectName) }
        }
    }

    private fun addProjectHeader(name: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 24, 0, 8)
        }
        val tv = TextView(this).apply {
            text = name
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF1E293B.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val icon = ImageView(this).apply {
            setImageResource(R.drawable.mail)
            layoutParams = LinearLayout.LayoutParams(48, 48)
        }
        row.addView(tv)
        row.addView(icon)
        projectContainer.addView(row)
        projectContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(0xFF1E293B.toInt())
        })
    }

    private fun addTaskRow(task: TaskItem, projectName: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 14, 0, 14)
        }
        val cb = CheckBox(this).apply {
            isChecked = task.isDone
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 12 }
        }
        val title = TextView(this).apply {
            text = task.title
            textSize = 14f
            setTextColor(if (task.isDone) 0xFF94A3B8.toInt() else 0xFF1E293B.toInt())
            if (task.isDone) paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        cb.setOnCheckedChangeListener { _, checked ->
            if (checked && !task.isDone) { TaskRepository.markDone(task); refreshList() }
            else if (!checked && task.isDone) { TaskRepository.markUndone(task); refreshList() }
        }
        row.addView(cb)
        row.addView(title)
        row.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Move \"${task.title}\" to trash?")
                .setPositiveButton("Move to Trash") { _, _ ->
                    TaskRepository.moveToTrash(task)
                    TaskRepository.projects[projectName]?.removeAll { it.id == task.id }
                    refreshList()
                }
                .setNegativeButton("Cancel", null).show()
            true
        }
        projectContainer.addView(row)
        projectContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(0xFFE2E8F0.toInt())
        })
    }

    // ── ADD TASK OVERLAY ──────────────────────────────────────────────────────

    private fun showAddTaskOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_add_task_options, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<LinearLayout>(R.id.optionNewTodo).setOnClickListener {
            dialog.dismiss(); showNewTodoOverlay()
        }
        view.findViewById<LinearLayout>(R.id.optionNewProject).setOnClickListener {
            dialog.dismiss(); showNewProjectOverlay()
        }
        dialog.show()
    }

    private fun showNewTodoOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_new_todo, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val titleInput  = view.findViewById<EditText>(R.id.edittextTodoTitle)
        val notesInput  = view.findViewById<EditText>(R.id.edittextTodoNotes)
        val closeBtn    = view.findViewById<ImageView>(R.id.imageviewCloseTodo)
        val saveBtn     = view.findViewById<Button>(R.id.buttonSaveTodo)
        val calIcon     = view.findViewById<ImageView>(R.id.imageviewCalendarIcon)
        val tagIcon     = view.findViewById<ImageView>(R.id.imageviewTagIcon)
        val projectIcon = view.findViewById<ImageView>(R.id.imageviewProjectIcon)

        var selectedDeadline: Long? = null
        var selectedTag = "Inbox"
        var selectedProject: String? = null

        closeBtn.setOnClickListener { dialog.dismiss() }

        calIcon.setOnClickListener {
            val cal = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, y, m, d ->
                val picked = java.util.Calendar.getInstance().apply {
                    set(y, m, d, 12, 0, 0); set(java.util.Calendar.MILLISECOND, 0)
                }
                selectedDeadline = picked.timeInMillis
                Toast.makeText(this, "Due: $d/${m+1}/$y", Toast.LENGTH_SHORT).show()
            }, cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        tagIcon.setOnClickListener {
            val tagView = layoutInflater.inflate(R.layout.dialog_tag_picker, null)
            val tagDialog = AlertDialog.Builder(this).setView(tagView).create()
            tagView.findViewById<LinearLayout>(R.id.optionTagInbox).setOnClickListener {
                selectedTag = "Inbox"; tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagUpcoming).setOnClickListener {
                selectedTag = "Upcoming"; tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagAnytime).setOnClickListener {
                selectedTag = "Anytime"; tagDialog.dismiss()
            }
            tagDialog.show()
        }

        projectIcon.setOnClickListener {
            val projView = layoutInflater.inflate(R.layout.dialog_project_picker, null)
            val projDialog = AlertDialog.Builder(this).setView(projView).create()
            val container = projView.findViewById<LinearLayout>(R.id.linearlayoutProjectList)
            projView.findViewById<ImageView>(R.id.imageviewCloseProject).setOnClickListener { projDialog.dismiss() }
            fun rebuild() {
                container.removeAllViews()
                TaskRepository.projects.keys.forEach { pname ->
                    val tv = TextView(this).apply {
                        text = pname; textSize = 15f; setTextColor(0xFF1E293B.toInt())
                        setPadding(0, 20, 0, 20)
                        setOnClickListener { selectedProject = pname; projDialog.dismiss() }
                    }
                    container.addView(tv)
                    container.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                        setBackgroundColor(0xFFE2E8F0.toInt())
                    })
                }
            }
            rebuild()
            projView.findViewById<TextView>(R.id.textviewAddNewProject).setOnClickListener {
                val input = EditText(this).apply { hint = "Project name" }
                AlertDialog.Builder(this).setTitle("New Project").setView(input)
                    .setPositiveButton("Create") { _, _ ->
                        val n = input.text.toString().trim()
                        if (n.isNotEmpty()) { TaskRepository.projects.getOrPut(n) { ArrayList() }; rebuild() }
                    }.setNegativeButton("Cancel", null).show()
            }
            projDialog.show()
        }

        saveBtn.setOnClickListener {
            val name = titleInput.text.toString().trim()
            if (name.isNotEmpty()) {
                TaskRepository.addTask(name, notesInput.text.toString().trim(), selectedTag, selectedDeadline, selectedProject)
                refreshList(); dialog.dismiss()
            } else Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showNewProjectOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_new_project, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val titleInput = view.findViewById<EditText>(R.id.edittextProjectTitle)
        val closeBtn   = view.findViewById<ImageView>(R.id.imageviewCloseProject)
        val saveBtn    = view.findViewById<Button>(R.id.buttonSaveProject)

        closeBtn.setOnClickListener { dialog.dismiss() }
        saveBtn.setOnClickListener {
            val name = titleInput.text.toString().trim()
            if (name.isNotEmpty()) {
                TaskRepository.projects.getOrPut(name) { ArrayList() }
                Toast.makeText(this, "Project \"$name\" created", Toast.LENGTH_SHORT).show()
                refreshList(); dialog.dismiss()
            } else Toast.makeText(this, "Enter a project name", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    // ── NAV ICONS ─────────────────────────────────────────────────────────────

    private fun setNavActive(active: String) {
        findViewById<ImageView>(R.id.imageviewNavTodo).setImageResource(
            if (active == "todo") R.drawable.todo_active else R.drawable.todo_not_active)
        findViewById<ImageView>(R.id.imageviewNavProjects).setImageResource(
            if (active == "projects") R.drawable.project_active else R.drawable.project_not_active)
        findViewById<ImageView>(R.id.imageviewNavCalendar).setImageResource(
            if (active == "calendar") R.drawable.calendar_active else R.drawable.calendar_not_active)
    }
}