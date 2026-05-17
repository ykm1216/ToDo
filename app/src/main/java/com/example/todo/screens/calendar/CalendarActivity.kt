package com.example.todo.screens.calendar

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
import android.view.WindowManager
import android.widget.*
import com.example.todo.R
import com.example.todo.data.TaskRepository
import com.example.todo.data.models.TaskItem
import com.example.todo.screens.dashboard.DashboardActivity
import com.example.todo.screens.profile.ProfileActivity
import com.example.todo.screens.projects.ProjectsActivity
import java.util.*

class CalendarActivity : Activity() {

    private lateinit var calendarContainer: LinearLayout
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarContainer = findViewById(R.id.linearlayoutCalendarContainer)
        searchInput       = findViewById(R.id.edittextSearch)

        setNavActive("calendar")

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refreshCalendar() }
        })

        findViewById<ImageButton>(R.id.imagebuttonAddTaskCalendar).setOnClickListener {
            showAddTaskOverlay()
        }

        // Nav
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
        findViewById<LinearLayout>(R.id.linearlayoutNavProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        setNavActive("calendar")
        refreshCalendar()
    }

    // ── CALENDAR RENDER ───────────────────────────────────────────────────────

    private fun refreshCalendar() {
        calendarContainer.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()

        val now = Calendar.getInstance()
        val thisYear = now.get(Calendar.YEAR)
        val startMonth = now.get(Calendar.MONTH)

        val monthNames = arrayOf(
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        )

        for (month in startMonth..11) {
            val allTasks = TaskRepository.allActiveTasks() + TaskRepository.doneTasks
            val tasksInMonth = allTasks.filter { task ->
                if (task.deadlineMillis == null) return@filter false
                val cal = Calendar.getInstance().apply { timeInMillis = task.deadlineMillis!! }
                cal.get(Calendar.YEAR) == thisYear && cal.get(Calendar.MONTH) == month
            }.filter { task ->
                query.isEmpty() || task.title.lowercase().contains(query)
            }

            if (query.isNotEmpty() && tasksInMonth.isEmpty()) continue

            // Month header
            val headerRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 48, 0, 22)
            }
            val monthTv = TextView(this).apply {
                text = "${monthNames[month]} $thisYear"
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(0xFF1E293B.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val calIcon = ImageView(this).apply {
                setImageResource(R.drawable.calendar_active)
                layoutParams = LinearLayout.LayoutParams(40, 40)
            }
            headerRow.addView(monthTv)
            headerRow.addView(calIcon)
            calendarContainer.addView(headerRow)

            calendarContainer.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(0xFF1E293B.toInt())
            })

            tasksInMonth.forEach { task -> addCalendarTaskRow(task) }
            if (tasksInMonth.isEmpty()) {
                calendarContainer.addView(TextView(this).apply {
                    text = "No tasks"
                    textSize = 13f
                    setTextColor(0xFF94A3B8.toInt())
                    setPadding(0, 8, 0, 8)
                })
            }
        }

        if (calendarContainer.childCount == 0 && query.isNotEmpty()) {
            calendarContainer.addView(TextView(this).apply {
                text = "No results for \"$query\""
                textSize = 14f
                setTextColor(0xFF94A3B8.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 68, 0, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            })

            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).also {
                it.bottomMargin = 8
            }
        }
    }

    private fun addCalendarTaskRow(task: TaskItem) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 12)
        }

        val cb = CheckBox(this).apply {
            isChecked = task.isDone
            // Prevent checkbox from consuming touches meant for the title
            isFocusable = false
            isFocusableInTouchMode = false
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
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        cb.setOnCheckedChangeListener { _, checked ->
            if (checked && !task.isDone) { TaskRepository.markDone(task); refreshCalendar() }
            else if (!checked && task.isDone) { TaskRepository.markUndone(task); refreshCalendar() }
        }

        // Tap title → edit task overlay
        title.setOnClickListener { showEditTaskOverlay(task) }

        row.addView(cb)
        row.addView(title)

        // Long press → trash
        row.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Move \"${task.title}\" to trash?")
                .setPositiveButton("Move to Trash") { _, _ ->
                    TaskRepository.moveToTrash(task); refreshCalendar()
                }
                .setNegativeButton("Cancel", null).show()
            true
        }

        calendarContainer.addView(row)
        calendarContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 32)
        })
    }

    // ── EDIT TASK OVERLAY ─────────────────────────────────────────────────────

    private fun showEditTaskOverlay(task: TaskItem) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_task, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(view).create()

        val titleInput  = view.findViewById<EditText>(R.id.edittextEditTitle)
        val notesInput  = view.findViewById<EditText>(R.id.edittextEditNotes)
        val closeBtn    = view.findViewById<ImageView>(R.id.imageviewCloseEdit)
        val saveBtn     = view.findViewById<Button>(R.id.buttonSaveEdit)
        val calIcon     = view.findViewById<ImageView>(R.id.imageviewEditCalendar)
        val tagIcon     = view.findViewById<ImageView>(R.id.imageviewEditTag)
        val projectIcon = view.findViewById<ImageView>(R.id.imageviewEditProject)

        titleInput.setText(task.title)
        notesInput.setText(task.notes)

        var selectedDeadline: Long? = task.deadlineMillis
        var selectedTag = task.tag ?: "Inbox"
        var selectedProject: String? = task.projectName

        closeBtn.setOnClickListener { dialog.dismiss() }

        calIcon.setOnClickListener {
            val cal = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, y, m, d ->
                val picked = java.util.Calendar.getInstance().apply {
                    set(y, m, d, 12, 0, 0); set(java.util.Calendar.MILLISECOND, 0)
                }
                selectedDeadline = picked.timeInMillis
                val ts = todayStart(); val te = todayEnd()
                selectedTag = when {
                    picked.timeInMillis < ts -> "Inbox"
                    picked.timeInMillis in ts..te -> "Today"
                    else -> "Upcoming"
                }
                Toast.makeText(this, "Due: $d/${m+1}/$y → $selectedTag", Toast.LENGTH_SHORT).show()
            }, cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        tagIcon.setOnClickListener {
            val tagView = layoutInflater.inflate(R.layout.dialog_tag_picker, null)
            val tagDialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(tagView).create()
            tagView.findViewById<LinearLayout>(R.id.optionTagInbox).setOnClickListener {
                selectedTag = "Inbox"; Toast.makeText(this, "Tag: Inbox", Toast.LENGTH_SHORT).show(); tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagUpcoming).setOnClickListener {
                selectedTag = "Upcoming"; Toast.makeText(this, "Tag: Upcoming", Toast.LENGTH_SHORT).show(); tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagAnytime).setOnClickListener {
                selectedTag = "Anytime"; Toast.makeText(this, "Tag: Anytime", Toast.LENGTH_SHORT).show(); tagDialog.dismiss()
            }
            tagDialog.formatAsCard()
        }

        projectIcon.setOnClickListener {
            showProjectPickerDialog { pname -> selectedProject = pname }
        }

        saveBtn.setOnClickListener {
            val newTitle = titleInput.text.toString().trim()
            val newNotes = notesInput.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                TaskRepository.updateTask(task, newTitle, newNotes, selectedTag, selectedDeadline, selectedProject)
                refreshCalendar()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.formatAsCard()
    }

    // ── ADD TASK OVERLAY ──────────────────────────────────────────────────────

    private fun showAddTaskOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_add_task_options, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(view).create()

        view.findViewById<LinearLayout>(R.id.optionNewTodo).setOnClickListener {
            dialog.dismiss(); showNewTodoOverlay()
        }
        view.findViewById<LinearLayout>(R.id.optionNewProject).setOnClickListener {
            dialog.dismiss(); showNewProjectOverlay()
        }
        dialog.formatAsCard()
    }

    private fun showNewTodoOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_new_todo, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(view).create()

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
            val cal = Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, y, m, d ->
                val picked = Calendar.getInstance().apply {
                    set(y, m, d, 12, 0, 0); set(Calendar.MILLISECOND, 0)
                }
                selectedDeadline = picked.timeInMillis
                val todayStart = todayStart(); val todayEnd = todayEnd()
                selectedTag = when {
                    picked.timeInMillis < todayStart -> "Inbox"
                    picked.timeInMillis in todayStart..todayEnd -> "Today"
                    else -> "Upcoming"
                }
                Toast.makeText(this, "Due: $d/${m+1}/$y", Toast.LENGTH_SHORT).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        tagIcon.setOnClickListener {
            val tagView = layoutInflater.inflate(R.layout.dialog_tag_picker, null)
            val tagDialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(tagView).create()
            tagView.findViewById<LinearLayout>(R.id.optionTagInbox).setOnClickListener {
                selectedTag = "Inbox"; tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagUpcoming).setOnClickListener {
                selectedTag = "Upcoming"; tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagAnytime).setOnClickListener {
                selectedTag = "Anytime"; tagDialog.dismiss()
            }
            tagDialog.formatAsCard()
        }

        projectIcon.setOnClickListener { showProjectPickerDialog { pname -> selectedProject = pname } }

        saveBtn.setOnClickListener {
            val name = titleInput.text.toString().trim()
            if (name.isNotEmpty()) {
                TaskRepository.addTask(name, notesInput.text.toString().trim(), selectedTag, selectedDeadline, selectedProject)
                refreshCalendar(); dialog.dismiss()
            } else Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show()
        }

        dialog.formatAsCard()
    }

    private fun showNewProjectOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_new_project, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(view).create()

        val titleInput = view.findViewById<EditText>(R.id.edittextProjectTitle)
        val closeBtn   = view.findViewById<ImageView>(R.id.imageviewCloseProject)
        val saveBtn    = view.findViewById<Button>(R.id.buttonSaveProject)

        closeBtn.setOnClickListener { dialog.dismiss() }
        saveBtn.setOnClickListener {
            val name = titleInput.text.toString().trim()
            if (name.isNotEmpty()) {
                TaskRepository.projects.getOrPut(name) { ArrayList() }
                Toast.makeText(this, "Project \"$name\" created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else Toast.makeText(this, "Enter a project name", Toast.LENGTH_SHORT).show()
        }
        dialog.formatAsCard()
    }

    // ── PROJECT PICKER ────────────────────────────────────────────────────────

    private fun showProjectPickerDialog(onPicked: (String) -> Unit) {
        val projView = layoutInflater.inflate(R.layout.dialog_project_picker, null)
        val projDialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(projView).create()
        val container  = projView.findViewById<LinearLayout>(R.id.linearlayoutProjectList)
        projView.findViewById<ImageView>(R.id.imageviewCloseProject).setOnClickListener { projDialog.dismiss() }

        fun rebuildList() {
            container.removeAllViews()
            TaskRepository.projects.keys.forEach { pname ->
                val tv = TextView(this).apply {
                    text = pname; textSize = 15f; setTextColor(0xFF1E293B.toInt())
                    setPadding(0, 24, 0, 20)
                    setOnClickListener { onPicked(pname); projDialog.dismiss() }
                }
                container.addView(tv)
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(0xFFE2E8F0.toInt())
                })
            }
        }

        rebuildList()

        // "+ Add New Project" opens proper dialog_new_project overlay
        projView.findViewById<TextView>(R.id.textviewAddNewProject).setOnClickListener {
            projDialog.dismiss()
            showNewProjectFromPicker(onPicked)
        }

        projDialog.formatAsCard()
    }

    private fun showNewProjectFromPicker(onPicked: (String) -> Unit) {
        val view = layoutInflater.inflate(R.layout.dialog_new_project, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(view).create()

        val titleInput = view.findViewById<EditText>(R.id.edittextProjectTitle)
        val closeBtn   = view.findViewById<ImageView>(R.id.imageviewCloseProject)
        val saveBtn    = view.findViewById<Button>(R.id.buttonSaveProject)

        closeBtn.setOnClickListener { dialog.dismiss() }
        saveBtn.setOnClickListener {
            val name = titleInput.text.toString().trim()
            if (name.isNotEmpty()) {
                TaskRepository.projects.getOrPut(name) { ArrayList() }
                onPicked(name)
                dialog.dismiss()
                Toast.makeText(this, "Project \"$name\" created & selected", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Enter a project name", Toast.LENGTH_SHORT).show()
        }
        dialog.formatAsCard()
    }

    // ── NAV ───────────────────────────────────────────────────────────────────

    private fun setNavActive(active: String) {
        findViewById<ImageView>(R.id.imageviewNavTodo).setImageResource(
            if (active == "todo") R.drawable.todo_active else R.drawable.todo_not_active)
        findViewById<ImageView>(R.id.imageviewNavProjects).setImageResource(
            if (active == "projects") R.drawable.project_active else R.drawable.project_not_active)
        findViewById<ImageView>(R.id.imageviewNavCalendar).setImageResource(
            if (active == "calendar") R.drawable.calendar_active else R.drawable.calendar_not_active)
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private fun AlertDialog.formatAsCard() {
        this.show()
        this.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val width = (this@CalendarActivity.resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun todayStart() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun todayEnd() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}