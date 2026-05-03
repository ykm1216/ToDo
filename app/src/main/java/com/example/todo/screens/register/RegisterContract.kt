package com.example.todo.screens.register

interface RegisterContract {
    interface View {
        fun showToast(message: String)
        fun navigateToLogin()
    }
    interface Presenter {
        fun onSignUpClicked(email: String, user: String, pass: String, confirm: String)
    }
}