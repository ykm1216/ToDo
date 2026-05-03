package com.example.todo

import android.app.Application
import com.example.todo.data.repositories.UserRepository


class TodoApp : Application() {

     val userRepository: UserRepository by lazy {
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        UserRepository(sharedPrefs)
    }
}