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
import android.view.WindowManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        projectContainer = findViewById(R.id.linearlayoutProjectContainer)
        searchInput      = findViewById(R.id.edittextSearchProjects)

        setNavActive("projects")

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refreshProjects() }
        })

        findViewById<ImageButton>(R.id.imagebuttonAddTaskProjects).setOnClickListener {
            showAddTaskOverlay()
        }

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
        refreshProjects()
    }

    // ── PROJECT LIST RENDER ───────────────────────────────────────────────────

    private fun refreshProjects() {
        projectContainer.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()

        if (TaskRepository.projects.isEmpty()) {
            if (query.isEmpty()) {
                projectContainer.addView(TextView(this).apply {
                    text = "No projects yet. Tap + to create one."
                    textSize = 14f
                    setTextColor(0xFF94A3B8.toInt())
                    gravity = Gravity.CENTER
                    setPadding(0, 48, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                })
            }
            return
        }

        var anyResults = false

        TaskRepository.projects.forEach { (projectName, tasks) ->
            val filteredTasks = if (query.isEmpty()) tasks
            else tasks.filter { it.title.lowercase().contains(query) }

            val projectNameMatches = projectName.lowercase().contains(query)
            if (query.isNotEmpty() && filteredTasks.isEmpty() && !projectNameMatches) return@forEach

            anyResults = true
            addProjectSection(projectName, tasks, filteredTasks)
        }

        if (!anyResults && query.isNotEmpty()) {
            projectContainer.addView(TextView(this).apply {
                text = "No results for \"$query\""
                textSize = 14f
                setTextColor(0xFF94A3B8.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 48, 0, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
    }

    private fun addProjectSection(projectName: String, allTasks: ArrayList<TaskItem>, shownTasks: List<TaskItem>) {
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 20, 0, 8)
        }

        val projectTv = TextView(this).apply {
            text = projectName
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF1E293B.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // ── CHANGED: pencil emoji → edit.png ──────────────────────────────────
        val editBtn = ImageView(this).apply {
            setImageResource(R.drawable.edit)
            layoutParams = LinearLayout.LayoutParams(44, 44).apply { marginStart = 12 }
            setOnClickListener { showEditProjectOverlay(projectName, allTasks) }
        }

        // ── CHANGED: trash emoji → x.png ──────────────────────────────────────
        val deleteBtn = ImageView(this).apply {
            setImageResource(R.drawable.x)
            layoutParams = LinearLayout.LayoutParams(44, 44).apply { marginStart = 8 }
            setOnClickListener {
                AlertDialog.Builder(this@ProjectsActivity)
                    .setTitle("Delete Project")
                    .setMessage("Delete project \"$projectName\" and move its tasks to Inbox?")
                    .setPositiveButton("Delete") { _, _ ->
                        allTasks.forEach { task ->
                            if (!TaskRepository.inboxTasks.contains(task) &&
                                !TaskRepository.doneTasks.contains(task) &&
                                !TaskRepository.trashTasks.contains(task)) {
                                task.projectName = null
                                TaskRepository.inboxTasks.add(0, task)
                            } else {
                                task.projectName = null
                            }
                        }
                        TaskRepository.projects.remove(projectName)
                        refreshProjects()
                    }
                    .setNegativeButton("Cancel", null).show()
            }
        }

        headerRow.addView(projectTv)
        headerRow.addView(editBtn)
        headerRow.addView(deleteBtn)
        projectContainer.addView(headerRow)

        projectContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
            setBackgroundColor(0xFF1E293B.toInt())
        })

        if (shownTasks.isEmpty()) {
            projectContainer.addView(TextView(this).apply {
                text = "No tasks"
                textSize = 13f
                setTextColor(0xFF94A3B8.toInt())
                setPadding(0, 8, 0, 8)
            })
        } else {
            shownTasks.forEach { task -> addProjectTaskRow(task) }
        }

        projectContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 16)
        })
    }

    private fun addProjectTaskRow(task: TaskItem) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 12)
        }

        val cb = CheckBox(this).apply {
            isChecked = task.isDone
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
            if (checked && !task.isDone) { TaskRepository.markDone(task); refreshProjects() }
            else if (!checked && task.isDone) { TaskRepository.markUndone(task); refreshProjects() }
        }

        title.setOnClickListener { showEditTaskOverlay(task) }

        row.addView(cb)
        row.addView(title)

        row.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Move \"${task.title}\" to trash?")
                .setPositiveButton("Move to Trash") { _, _ ->
                    TaskRepository.moveToTrash(task); refreshProjects()
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

    // ── EDIT PROJECT OVERLAY ──────────────────────────────────────────────────

    private fun showEditProjectOverlay(projectName: String, tasks: ArrayList<TaskItem>) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_project, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(view).create()

        val titleInput = view.findViewById<EditText>(R.id.edittextEditProjectTitle)
        val closeBtn   = view.findViewById<ImageView>(R.id.imageviewCloseEditProject)
        val saveBtn    = view.findViewById<Button>(R.id.buttonSaveEditProject)

        titleInput.setText(projectName)

        closeBtn.setOnClickListener { dialog.dismiss() }

        saveBtn.setOnClickListener {
            val newName = titleInput.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Project name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newName == projectName) { dialog.dismiss(); return@setOnClickListener }
            if (TaskRepository.projects.containsKey(newName)) {
                Toast.makeText(this, "A project with that name already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newProjects = LinkedHashMap<String, ArrayList<TaskItem>>()
            TaskRepository.projects.forEach { (k, v) ->
                if (k == projectName) {
                    v.forEach { it.projectName = newName }
                    newProjects[newName] = v
                } else {
                    newProjects[k] = v
                }
            }
            TaskRepository.projects.clear()
            TaskRepository.projects.putAll(newProjects)

            refreshProjects()
            dialog.dismiss()
            Toast.makeText(this, "Renamed to \"$newName\"", Toast.LENGTH_SHORT).show()
        }

        dialog.formatAsCard()
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
                refreshProjects()
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
            tagDialog.formatAsCard()
        }

        projectIcon.setOnClickListener { showProjectPickerDialog { pname -> selectedProject = pname } }

        saveBtn.setOnClickListener {
            val newTitle = titleInput.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                TaskRepository.addTask(newTitle, notesInput.text.toString().trim(), selectedTag, selectedDeadline, selectedProject)
                refreshProjects()
                dialog.dismiss()
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
                refreshProjects()
                dialog.dismiss()
                Toast.makeText(this, "Project \"$name\" created", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Enter a project name", Toast.LENGTH_SHORT).show()
        }
        dialog.formatAsCard()
    }

    // ── PROJECT PICKER ────────────────────────────────────────────────────────

    private fun showProjectPickerDialog(onPicked: (String) -> Unit) {
        val projView   = layoutInflater.inflate(R.layout.dialog_project_picker, null)
        val projDialog = AlertDialog.Builder(this, R.style.CustomDialogCardTheme).setView(projView).create()
        val container  = projView.findViewById<LinearLayout>(R.id.linearlayoutProjectList)
        projView.findViewById<ImageView>(R.id.imageviewCloseProject).setOnClickListener { projDialog.dismiss() }

        fun rebuildList() {
            container.removeAllViews()
            TaskRepository.projects.keys.forEach { pname ->
                val tv = TextView(this).apply {
                    text = pname; textSize = 15f; setTextColor(0xFF1E293B.toInt())
                    setPadding(0, 20, 0, 20)
                    setOnClickListener {
                        onPicked(pname)
                        Toast.makeText(this@ProjectsActivity, "Project: $pname", Toast.LENGTH_SHORT).show()
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
                refreshProjects()
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
            val width = (this@ProjectsActivity.resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
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