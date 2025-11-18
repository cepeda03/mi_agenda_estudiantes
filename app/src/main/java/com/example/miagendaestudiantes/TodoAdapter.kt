package com.example.miagendaestudiantes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoAdapter(
    private val onTodoChecked: (Todo, Boolean) -> Unit,
    private val onEditClicked: (Todo) -> Unit,
    private val onDisableClicked: (Todo) -> Unit,
    private val onDeleteClicked: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private val todos = mutableListOf<Todo>()

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val btnEditar: Button = itemView.findViewById(R.id.btnEditar)
        val btnDeshabilitar: Button = itemView.findViewById(R.id.btnDeshabilitar)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentTodo = todos[position]

        // -------- FECHA (solo día/mes/año) ----------
        val fechaTexto = if (currentTodo.createdAt != 0L) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(currentTodo.createdAt))
        } else {
            ""
        }

        // Texto + fecha en 2 líneas
        holder.textView.text = if (fechaTexto.isNotEmpty()) {
            "${currentTodo.text}\n$fechaTexto"
        } else {
            currentTodo.text
        }

        // -------- CHECKBOX (sin tachar texto) ----------
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = currentTodo.completed

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != currentTodo.completed) {
                Log.d("TodoAdapter", "Checkbox changed for ${currentTodo.text}: $isChecked")
                onTodoChecked(currentTodo, isChecked)
            }
        }

        // -------- BOTONES ----------
        // Editar
        holder.btnEditar.setOnClickListener {
            onEditClicked(currentTodo)
        }

        // Deshabilitar / habilitar
        holder.btnDeshabilitar.text = if (currentTodo.enabled) "Off" else "On"
        holder.btnDeshabilitar.setOnClickListener {
            onDisableClicked(currentTodo)
        }

        // Eliminar
        holder.btnEliminar.setOnClickListener {
            onDeleteClicked(currentTodo)
        }
    }

    override fun getItemCount(): Int = todos.size

    fun updateTodos(newTodos: List<Todo>) {
        Log.d("TodoAdapter", "Updating todos. New count: ${newTodos.size}")
        todos.clear()
        todos.addAll(newTodos)
        notifyDataSetChanged()
    }

    fun removeTodo(todoId: String) {
        val index = todos.indexOfFirst { it.id == todoId }
        if (index != -1) {
            todos.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}

