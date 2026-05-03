package com.example.todo.screens.profile

interface ProfileContract {
    interface View {
        fun displayProfile(username: String, email: String)
        fun navigateToLogin()
        fun closeScreen()
    }
    interface Presenter {
        fun loadUserData()
        fun onLogoutClicked()
        fun onBackClicked()
    }
}