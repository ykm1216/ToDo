package com.example.todo.data

import com.example.todo.data.models.TaskItem

object TaskRepository {

    // Projects: name -> list of tasks
    val projects = LinkedHashMap<String, ArrayList<TaskItem>>()

    val todayTasks    = ArrayList<TaskItem>()
    val inboxTasks    = ArrayList<TaskItem>()
    val upcomingTasks = ArrayList<TaskItem>()
    val anytimeTasks  = ArrayList<TaskItem>()
    val doneTasks     = ArrayList<TaskItem>()
    val trashTasks    = ArrayList<TaskItem>()

    private var nextId = 1

    fun newId() = nextId++

    /** Routes a new task to the correct bucket based on tag + deadline */
    fun addTask(title: String, notes: String, tag: String, deadlineMillis: Long?, projectName: String?) {
        val id = newId()
        val task = TaskItem(id = id, title = title, type = tag, iconRes = com.example.todo.R.drawable.check_icon,
            notes = notes, deadlineMillis = deadlineMillis, tag = tag, projectName = projectName)

        if (projectName != null) {
            val list = projects.getOrPut(projectName) { ArrayList() }
            list.add(task)
        }

        val todayStart = todayStart()
        val todayEnd   = todayEnd()

        when {
            deadlineMillis != null && deadlineMillis < todayStart -> inboxTasks.add(0, task)   // overdue → inbox
            deadlineMillis != null && deadlineMillis in todayStart..todayEnd -> todayTasks.add(0, task)
            tag == "Inbox"    -> inboxTasks.add(0, task)
            tag == "Upcoming" -> upcomingTasks.add(0, task)
            tag == "Anytime"  -> anytimeTasks.add(0, task)
            else              -> inboxTasks.add(0, task)   // default
        }
    }

    fun markDone(task: TaskItem) {
        removeFromAllBuckets(task.id)
        task.isDone = true
        doneTasks.add(0, task)
        // also update in project
        task.projectName?.let { pname ->
            projects[pname]?.find { it.id == task.id }?.isDone = true
        }
    }

    fun markUndone(task: TaskItem) {
        doneTasks.removeAll { it.id == task.id }
        task.isDone = false
        // re-route
        val todayStart = todayStart()
        val todayEnd   = todayEnd()
        when {
            task.deadlineMillis != null && task.deadlineMillis!! < todayStart -> inboxTasks.add(0, task)
            task.deadlineMillis != null && task.deadlineMillis!! in todayStart..todayEnd -> todayTasks.add(0, task)
            task.tag == "Inbox"    -> inboxTasks.add(0, task)
            task.tag == "Upcoming" -> upcomingTasks.add(0, task)
            task.tag == "Anytime"  -> anytimeTasks.add(0, task)
            else                   -> inboxTasks.add(0, task)
        }
        task.projectName?.let { pname ->
            projects[pname]?.find { it.id == task.id }?.isDone = false
        }
    }

    fun moveToTrash(task: TaskItem) {
        removeFromAllBuckets(task.id)
        trashTasks.add(0, task)
    }

    fun restoreFromTrash(task: TaskItem) {
        trashTasks.removeAll { it.id == task.id }
        addTaskObject(task)
    }

    private fun addTaskObject(task: TaskItem) {
        val todayStart = todayStart()
        val todayEnd   = todayEnd()
        when {
            task.deadlineMillis != null && task.deadlineMillis!! < todayStart -> inboxTasks.add(0, task)
            task.deadlineMillis != null && task.deadlineMillis!! in todayStart..todayEnd -> todayTasks.add(0, task)
            task.tag == "Inbox"    -> inboxTasks.add(0, task)
            task.tag == "Upcoming" -> upcomingTasks.add(0, task)
            task.tag == "Anytime"  -> anytimeTasks.add(0, task)
            else                   -> inboxTasks.add(0, task)
        }
    }

    private fun removeFromAllBuckets(id: Int) {
        todayTasks.removeAll    { it.id == id }
        inboxTasks.removeAll    { it.id == id }
        upcomingTasks.removeAll { it.id == id }
        anytimeTasks.removeAll  { it.id == id }
        doneTasks.removeAll     { it.id == id }
    }

    fun allActiveTasks(): List<TaskItem> =
        todayTasks + inboxTasks + upcomingTasks + anytimeTasks

    private fun todayStart(): Long {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun todayEnd(): Long {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 23)
        c.set(java.util.Calendar.MINUTE, 59)
        c.set(java.util.Calendar.SECOND, 59)
        c.set(java.util.Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }
}