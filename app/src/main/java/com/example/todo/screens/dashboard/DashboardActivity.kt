package com.example.todo.screens.dashboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.example.todo.screens.calendar.CalendarActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.todo.R
import com.example.todo.data.models.TaskItem
import com.example.todo.adapter.TaskAdapter
import com.example.todo.screens.profile.ProfileActivity

class DashboardActivity : Activity() {

    // Concepts: ArrayList and Custom Adapter declaration
    private lateinit var taskList: ArrayList<TaskItem>
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var welcomeCard: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Initialize UI Components
        val textViewUser = findViewById<TextView>(R.id.textViewUser)
        val navProfile = findViewById<LinearLayout>(R.id.linearlayoutNavProfile)
        val addTaskBtn = findViewById<ImageButton>(R.id.imagebuttonAddTask)
        welcomeCard = findViewById(R.id.linearlayoutWelcomeCard)

        // 2. Setup ArrayList and ListView
        val listView = findViewById<ListView>(R.id.listViewTasks)

        taskList = ArrayList()
        taskAdapter = TaskAdapter(this, taskList)
        listView.adapter = taskAdapter

        // 3. Load User Data
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        textViewUser.text = "Welcome ${sharedPref.getString("username", "User")}"

        // 4. ADD ITEM (Custom ListView Add) - Puts new task at the TOP
        addTaskBtn.setOnClickListener {
            // Show the options bottom sheet first (New To-Do / New Project / New Area)
            val optionsView = layoutInflater.inflate(R.layout.dialog_add_task_options, null)
            val optionsDialog = AlertDialog.Builder(this)
                .setView(optionsView)
                .create()

            optionsView.findViewById<LinearLayout>(R.id.optionNewTodo).setOnClickListener {
                optionsDialog.dismiss()
                showNewTodoDialog()
            }

            optionsView.findViewById<LinearLayout>(R.id.optionNewProject).setOnClickListener {
                optionsDialog.dismiss()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("New Project")
                val input = EditText(this)
                input.hint = "Project name"
                builder.setView(input)
                builder.setPositiveButton("Create") { _, _ ->
                    val name = input.text.toString()
                    if (name.isNotEmpty()) {
                        val newTask = TaskItem(id = taskList.size, title = "📁 $name", type = "Project", iconRes = R.drawable.check_icon)
                        taskList.add(0, newTask)
                        taskAdapter.notifyDataSetChanged()
                        welcomeCard.visibility = View.GONE
                    }
                }
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
                builder.setPositiveButton("Create") { _, _ ->
                    val name = input.text.toString()
                    if (name.isNotEmpty()) {
                        val newTask = TaskItem(id = taskList.size, title = "🗂 $name", type = "Area", iconRes = R.drawable.check_icon)
                        taskList.add(0, newTask)
                        taskAdapter.notifyDataSetChanged()
                        welcomeCard.visibility = View.GONE
                    }
                }
                builder.setNegativeButton("Cancel", null)
                builder.show()
            }

            optionsDialog.show()
        }


        // 5. CLICK ITEM (Custom ListView Click) - Moves completed task to the BOTTOM
        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedTask = taskList[position]

            if (!clickedTask.title.contains("(DONE)")) {
                // Logic: Remove from current spot
                taskList.removeAt(position)

                val updatedTask = TaskItem(
                    id = clickedTask.id,
                    title = "${clickedTask.title} (DONE)",
                    type = clickedTask.type,
                    iconRes = clickedTask.iconRes
                )

                // Logic: Add to the very bottom
                taskList.add(updatedTask)
                taskAdapter.notifyDataSetChanged()

                Toast.makeText(this, "Task moved to bottom", Toast.LENGTH_SHORT).show()
            }
        }
        // Section header clicks → open sub-list views
        val sectionToday = findViewById<TextView>(R.id.textviewSectionToday)
        val sectionUpcoming = findViewById<TextView>(R.id.textviewSectionUpcoming)
        val sectionAnytime = findViewById<TextView>(R.id.textviewSectionAnytime)
        val sectionDone = findViewById<TextView>(R.id.textviewSectionDone)

        sectionToday.setOnClickListener {
            val intent = Intent(this, SectionListActivity::class.java)
            intent.putExtra("section_title", "Today")
            startActivity(intent)
        }

        sectionUpcoming.setOnClickListener {
            val intent = Intent(this, SectionListActivity::class.java)
            intent.putExtra("section_title", "Upcoming")
            startActivity(intent)
        }

        sectionAnytime.setOnClickListener {
            val intent = Intent(this, SectionListActivity::class.java)
            intent.putExtra("section_title", "Anytime")
            startActivity(intent)
        }

        sectionDone.setOnClickListener {
            val intent = Intent(this, SectionListActivity::class.java)
            intent.putExtra("section_title", "Done")
            startActivity(intent)
        }

        val trashBtn = findViewById<TextView>(R.id.textviewTrashButton)
        trashBtn.setOnClickListener {
            startActivity(Intent(this, TrashActivity::class.java))
        }

        val navCalendar = findViewById<LinearLayout>(R.id.linearlayoutNavCalendar)
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

        // 6. REMOVE ITEM (Custom ListView Long Click) - Deletes task completely
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Task")
            builder.setMessage("Remove this task from your list?")
            builder.setPositiveButton("Remove") { _, _ ->
                // Logic: Remove from ArrayList
                taskList.removeAt(position)
                taskAdapter.notifyDataSetChanged()

                if (taskList.isEmpty()) welcomeCard.visibility = View.VISIBLE
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
            true
        }

        // Navigation
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }


    }
    private fun showNewTodoDialog() {
        val todoView = layoutInflater.inflate(R.layout.dialog_new_todo, null)
        val todoDialog = AlertDialog.Builder(this)
            .setView(todoView)
            .create()

        val titleInput = todoView.findViewById<EditText>(R.id.edittextTodoTitle)
        val closeBtn = todoView.findViewById<ImageView>(R.id.imageviewCloseTodo)
        val saveBtn = todoView.findViewById<Button>(R.id.buttonSaveTodo)
        val calendarIcon = todoView.findViewById<ImageView>(R.id.imageviewCalendarIcon)

        closeBtn.setOnClickListener {
            todoDialog.dismiss()
        }

        // Calendar date picker when calendar icon is tapped
        calendarIcon.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val datePicker = android.app.DatePickerDialog(
                this,
                { _, year, month, day ->
                    // Store or display selected date — attach to title as label for now
                    val dateLabel = "$day/${month + 1}/$year"
                    Toast.makeText(this, "Due: $dateLabel", Toast.LENGTH_SHORT).show()
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }



        saveBtn.setOnClickListener {
            val taskName = titleInput.text.toString().trim()
            if (taskName.isNotEmpty()) {
                val newTask = TaskItem(
                    id = taskList.size,
                    title = taskName,
                    type = "Task",
                    iconRes = R.drawable.check_icon
                )
                taskList.add(0, newTask)
                taskAdapter.notifyDataSetChanged()
                welcomeCard.visibility = View.GONE
                todoDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a task name", Toast.LENGTH_SHORT).show()
            }
        }

        todoDialog.show()
    }



}