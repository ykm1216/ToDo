package com.example.todo.screens.login

import com.example.todo.data.repositories.UserRepository

class LoginPresenter(
    private var view: LoginContract.View?,
    private val repository: UserRepository
) : LoginContract.Presenter {

    override fun initAutoFill() {
        val user = repository.getUser()
        if (user.username.isNotEmpty()) {
            view?.setAutoFill(user.username, user.password)
        }
    }

    override fun onLoginClicked(user: String, pass: String) {
        val savedUser = repository.getUser()
        if (user.isEmpty() || pass.isEmpty()) {
            view?.showToast("Please enter all fields")
        } else if (user == savedUser.username && pass == savedUser.password) {
            view?.navigateToDashboard()
        } else {
            view?.showToast("Invalid Username or Password")
        }
    }

    override fun onRegisterClicked() { view?.navigateToRegister() }
}