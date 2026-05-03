package com.example.todo.data.repositories

import android.content.SharedPreferences
import com.example.todo.data.models.User

class UserRepository(private val sharedPrefs: SharedPreferences) {

    fun saveUser(user: User) {
        sharedPrefs.edit().apply {
            putString("email", user.email)
            putString("username", user.username)
            putString("password", user.password)
            apply()
        }
    }

    fun getUser(): User {
        return User(
            sharedPrefs.getString("email", "") ?: "",
            sharedPrefs.getString("username", "") ?: "",
            sharedPrefs.getString("password", "") ?: ""
        )
    }

    fun clearSession() {
        // Keep credentials for auto-fill but clear session if needed
    }
}