package com.example.miagendaestudiantes

data class Todo(
    var id: String = "",
    var text: String = "",
    var completed: Boolean = false,
    var userId: String = "",
    var enabled: Boolean = true,
    var createdAt: Long = 0L,
    var finalDate: String = ""
)
