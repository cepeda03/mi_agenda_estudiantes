package com.example.miagendaestudiantes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(

    private val onTodoChecked: (Todo, Boolean) -> Unit,
    private val onEditClicked: (Todo) -> Unit,
    private val onDisableClicked: (Todo) -> Unit,
    private val onDeleteClicked: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private val todos = mutableListOf<Todo>()

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
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


        val fechaTexto: String = currentTodo.finalDate

        holder.textView.text = if (fechaTexto.isNotEmpty()) {

            "${currentTodo.text}\n$fechaTexto"
        } else {
            currentTodo.text
        }


        holder.btnEditar.setOnClickListener {
            onEditClicked(currentTodo)
        }

        holder.btnDeshabilitar.text = if (currentTodo.enabled) "Off" else "On"
        holder.btnDeshabilitar.setOnClickListener {
            onDisableClicked(currentTodo)
        }

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
