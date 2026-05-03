package com.example.todo.screens.login

interface LoginContract {
    interface View {
        fun showToast(message: String)
        fun navigateToDashboard()
        fun navigateToRegister()
        fun setAutoFill(user: String, pass: String)
    }
    interface Presenter {
        fun initAutoFill()
        fun onLoginClicked(user: String, pass: String)
        fun onRegisterClicked()
    }
}