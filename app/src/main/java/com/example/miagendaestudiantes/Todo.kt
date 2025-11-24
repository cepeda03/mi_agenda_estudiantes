package com.example.miagendaestudiantes

data class Todo(
    var id: String = "",
    var text: String = "",
    var enabled: Boolean = true,
    var createdAt: Long = 0L,
    var completed: Boolean = false,
    var finalDate: String = ""
)
