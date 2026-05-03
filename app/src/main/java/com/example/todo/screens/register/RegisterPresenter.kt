package com.example.todo.screens.register

import com.example.todo.data.models.User
import com.example.todo.data.repositories.UserRepository

class RegisterPresenter(
    private var view: RegisterContract.View?,
    private val repository: UserRepository
) : RegisterContract.Presenter {

    override fun onSignUpClicked(email: String, user: String, pass: String, confirm: String) {
        if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            view?.showToast("Please fill in all fields")
            return
        }
        if (pass != confirm) {
            view?.showToast("Passwords do not match")
            return
        }

        repository.saveUser(User(email, user, pass))
        view?.showToast("Registration Successful!")
        view?.navigateToLogin()
    }
}