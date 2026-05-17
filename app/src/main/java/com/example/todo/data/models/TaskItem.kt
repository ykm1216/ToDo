package com.example.todo.data.models

data class TaskItem(
    val id: Int,
    var title: String,
    var type: String = "Task",
    val iconRes: Int,
    var notes: String = "",
    var deadlineMillis: Long? = null,
    var tag: String = "Inbox",
    var projectName: String? = null,
    var isDone: Boolean = false
)