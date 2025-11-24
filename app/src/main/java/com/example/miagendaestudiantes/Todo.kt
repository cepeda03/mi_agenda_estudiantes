package com.example.miagendaestudiantes

data class Todo(
    val id: String = "",
    val text: String = "",
    var finalDate: String = "",
    val completed: Boolean = false,
    val userId: String = "",
    val enabled: Boolean = true,
    val createdAt: Long = 0L
)
