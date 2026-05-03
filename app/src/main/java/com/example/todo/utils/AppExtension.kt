package com.example.todo.utils

import android.content.Context
import android.view.View
import android.widget.Toast

//Toast
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

//view visibility
var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

//view
fun View.gone() {
    this.visibility = View.GONE
}