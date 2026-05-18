package com.example.todo.screens.dashboard

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
import com.example.todo.screens.calendar.CalendarActivity
import com.example.todo.screens.profile.ProfileActivity
import com.example.todo.screens.projects.ProjectsActivity

class DashboardActivity : Activity() {

    private lateinit var welcomeCard: LinearLayout
    private lateinit var taskContainer: LinearLayout
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        welcomeCard   = findViewById(R.id.linearlayoutWelcomeCard)
        taskContainer = findViewById(R.id.linearlayoutTaskContainer)
        searchInput   = findViewById(R.id.edittextSearch)

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        findViewById<TextView>(R.id.textViewUser).text =
            "Welcome ${sharedPref.getString("username", "User")}"

        setNavActive("todo")

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refreshList() }
        })

        findViewById<ImageButton>(R.id.imagebuttonAddTask).setOnClickListener { showAddTaskOverlay() }

        findViewById<TextView>(R.id.textviewTrashButton).setOnClickListener {
            startActivity(Intent(this, TrashActivity::class.java))
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
        findViewById<LinearLayout>(R.id.linearlayoutNavProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        setNavActive("todo")
        refreshList()
    }

    // ── RENDER ────────────────────────────────────────────────────────────────

    private fun refreshList() {
        taskContainer.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()

        val hasAny = TaskRepository.allActiveTasks().isNotEmpty() || TaskRepository.doneTasks.isNotEmpty()

        welcomeCard.visibility = if (!hasAny && query.isEmpty()) View.VISIBLE else View.GONE

        // Hide trash button while searching
        findViewById<TextView>(R.id.textviewTrashButton).visibility =
            if (query.isEmpty()) View.VISIBLE else View.GONE

        if (query.isEmpty()) {
            renderSection("Today",    TaskRepository.todayTasks)
            renderSection("Inbox",    TaskRepository.inboxTasks)
            renderSection("Upcoming", TaskRepository.upcomingTasks)
            renderSection("Anytime",  TaskRepository.anytimeTasks)
            renderSection("Done",     TaskRepository.doneTasks)
        } else {
            val all = (TaskRepository.allActiveTasks() + TaskRepository.doneTasks)
                .filter { it.title.lowercase().contains(query) }
            all.forEach { addTaskRow(it) }
            if (all.isEmpty()) addEmptyLabel("No results for \"$query\"")
        }
        taskContainer.requestLayout()
    }

    private fun renderSection(label: String, list: ArrayList<TaskItem>) {
        addSectionHeader(label)
        if (list.isEmpty()) {
            taskContainer.addView(TextView(this).apply {
                text = "No tasks"
                textSize = 13f
                setTextColor(0xFF94A3B8.toInt())
                setPadding(0, 6, 0, 6)
            })
        } else {
            list.forEach { addTaskRow(it) }
        }
        addDivider()
    }

    private fun addSectionHeader(label: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 20, 0, 6)
        }
        val tv = TextView(this).apply {
            text = label
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF1E293B.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        // ── ONLY CHANGE: today.png for Today, done.png for Done ───────────────
        val iconRes = when (label) {
            "Today"    -> R.drawable.today
            "Inbox"    -> R.drawable.mail
            "Upcoming" -> R.drawable.calendar_active
            "Anytime"  -> R.drawable.calendar_active
            "Done"     -> R.drawable.done
            else       -> R.drawable.check_icon
        }
        val icon = ImageView(this).apply {
            setImageResource(iconRes)
            layoutParams = LinearLayout.LayoutParams(36, 36)
        }
        row.addView(tv)
        row.addView(icon)
        taskContainer.addView(row)
    }

    private fun addTaskRow(task: TaskItem) {
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
            ).apply { marginEnd = 16 }
        }

        val title = TextView(this).apply {
            text = task.title
            textSize = 15f
            setTextColor(if (task.isDone) 0xFF94A3B8.toInt() else 0xFF1E293B.toInt())
            if (task.isDone) paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        cb.setOnCheckedChangeListener { _, checked ->
            if (checked && !task.isDone) {
                TaskRepository.markDone(task); refreshList()
            } else if (!checked && task.isDone) {
                TaskRepository.markUndone(task); refreshList()
            }
        }

        title.setOnClickListener { showEditTaskOverlay(task) }

        row.addView(cb)
        row.addView(title)

        row.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Move \"${task.title}\" to trash?")
                .setPositiveButton("Move to Trash") { _, _ ->
                    TaskRepository.moveToTrash(task); refreshList()
                }
                .setNegativeButton("Cancel", null).show()
            true
        }

        taskContainer.addView(row)
        taskContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(0xFFE2E8F0.toInt())
        })
    }

    private fun addDivider() {
        taskContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
            setBackgroundColor(0xFFE2E8F0.toInt())
        })
    }

    private fun addEmptyLabel(msg: String) {
        taskContainer.addView(TextView(this).apply {
            text = msg; textSize = 14f; setTextColor(0xFF94A3B8.toInt())
            gravity = Gravity.CENTER; setPadding(0, 48, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        })
    }

    private fun AlertDialog.formatAsCustomCard(activity: Activity, widthPercentage: Double = 0.90) {
        this.show()
        this.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val width = (activity.resources.displayMetrics.widthPixels * widthPercentage).toInt()
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
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
            tagDialog.formatAsCustomCard(this)
        }

        projectIcon.setOnClickListener {
            showProjectPickerDialog { pname -> selectedProject = pname }
        }

        saveBtn.setOnClickListener {
            val newTitle = titleInput.text.toString().trim()
            val newNotes = notesInput.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                TaskRepository.updateTask(task, newTitle, newNotes, selectedTag, selectedDeadline, selectedProject)
                refreshList()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.formatAsCustomCard(this)
    }

    // ── ADD TASK OVERLAY ──────────────────────────────────────────────────────

    private fun showAddTaskOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_add_task_options, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(view).create()

        view.findViewById<LinearLayout>(R.id.optionNewTodo).setOnClickListener {
            dialog.dismiss()
            showNewTodoOverlay()
        }
        view.findViewById<LinearLayout>(R.id.optionNewProject).setOnClickListener {
            dialog.dismiss()
            showNewProjectOverlay()   // ← calls the corrected method above
        }

        dialog.formatAsCustomCard(this)
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
                selectedTag = "Inbox"; tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagUpcoming).setOnClickListener {
                selectedTag = "Upcoming"; tagDialog.dismiss()
            }
            tagView.findViewById<LinearLayout>(R.id.optionTagAnytime).setOnClickListener {
                selectedTag = "Anytime"; tagDialog.dismiss()
            }
            tagDialog.formatAsCustomCard(this)
        }

        projectIcon.setOnClickListener { showProjectPickerDialog { pname -> selectedProject = pname } }

        saveBtn.setOnClickListener {
            val newTitle = titleInput.text.toString().trim()
            val newNotes = notesInput.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                TaskRepository.addTask(newTitle, newNotes, selectedTag, selectedDeadline, selectedProject)
                refreshList()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.formatAsCustomCard(this)
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
                refreshList()          // ← mirrors Projects: refreshes the list
                dialog.dismiss()       // ← dismiss BEFORE toast, same order as Projects
                Toast.makeText(this, "Project \"$name\" created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter a project name", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.formatAsCustomCard(this)   // Dashboard uses formatAsCustomCard, not formatAsCard
    }

    private fun showProjectPickerDialog(onPicked: (String) -> Unit) {
        val projView = layoutInflater.inflate(R.layout.dialog_project_picker, null)
        val projDialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(projView).create()
        val container = projView.findViewById<LinearLayout>(R.id.linearlayoutProjectList)
        projView.findViewById<ImageView>(R.id.imageviewCloseProject).setOnClickListener { projDialog.dismiss() }
        fun rebuildList() {
            container.removeAllViews()
            TaskRepository.projects.keys.forEach { pname ->
                val tv = TextView(this).apply {
                    text = pname; textSize = 15f; setTextColor(0xFF1E293B.toInt())
                    setPadding(0, 20, 0, 20)
                    setOnClickListener {
                        onPicked(pname)
                        Toast.makeText(this@DashboardActivity, "Project: $pname", Toast.LENGTH_SHORT).show()
                        projDialog.dismiss()
                    }
                }
                container.addView(tv)
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(0xFFE2E8F0.toInt())
                })
            }
        }
        rebuildList()
        projView.findViewById<TextView>(R.id.textviewAddNewProject).setOnClickListener {
            val input = EditText(this).apply { hint = "Project name" }
            AlertDialog.Builder(this).setTitle("New Project").setView(input)
                .setPositiveButton("Create") { _, _ ->
                    val n = input.text.toString().trim()
                    if (n.isNotEmpty()) { TaskRepository.projects.getOrPut(n) { ArrayList() }; rebuildList() }
                }.setNegativeButton("Cancel", null).show()
        }
        projDialog.formatAsCustomCard(this)
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

    private fun todayStart() = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun todayEnd() = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59); set(java.util.Calendar.MILLISECOND, 999)
    }.timeInMillis
}