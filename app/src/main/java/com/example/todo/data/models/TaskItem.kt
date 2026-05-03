package com.example.todo.data.models

// Notice the "data class" keyword and the parentheses () instead of {}
data class TaskItem(
    val id: Int,
    val title: String,
    val type: String,
    val iconRes: Int
)