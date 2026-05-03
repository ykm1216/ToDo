package com.example.todo.screens.profile

import com.example.todo.data.repositories.UserRepository

class ProfilePresenter(
    private var view: ProfileContract.View?,
    private val repository: UserRepository
) : ProfileContract.Presenter {

    override fun loadUserData() {
        val user = repository.getUser()
        view?.displayProfile(user.username, user.email)
    }

    override fun onLogoutClicked() {
        view?.navigateToLogin()
    }

    override fun onBackClicked() {
        view?.closeScreen()
    }
}