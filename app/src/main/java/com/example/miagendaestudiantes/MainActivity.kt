package com.example.miagendaestudiantes

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var todoEditText: EditText
    private lateinit var addButton: Button
    private lateinit var signOutButton: Button

    // UI/State
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)
        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadTodos()

        Log.d(TAG, "Usuario autenticado: ${auth.currentUser?.email}")
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        todoEditText = findViewById(R.id.todoEditText)
        addButton = findViewById(R.id.addButton)
        signOutButton = findViewById(R.id.signOutButton)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        todoAdapter = TodoAdapter(
            onTodoChecked = { todo, isChecked ->
                // Ya no hay checkbox en la UI, pero dejamos la función por compatibilidad
                updateTodoCompleted(todo, isChecked)
            },
            onEditClicked = { todo ->
                editarTodo(todo)
            },
            onDisableClicked = { todo ->
                deshabilitarTodo(todo)
            },
            onDeleteClicked = { todo ->
                eliminarTodo(todo)
            }
        )

        recyclerView.adapter = todoAdapter
    }

    private fun setupClickListeners() {
        addButton.setOnClickListener { addNewTodo() }
        signOutButton.setOnClickListener { signOut() }
    }


    private fun addNewTodo() {
        val todoText = todoEditText.text.toString().trim()
        if (todoText.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una tarea", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        val todo = Todo(
            id = "",
            text = todoText,
            completed = false,
            userId = userId,
            enabled = true,
            createdAt = now,
            finalDate = ""
        )

        Log.d(TAG, "Agregando nuevo todo: $todoText")

        db.collection("todos")
            .add(todo)
            .addOnSuccessListener {
                todoEditText.text.clear()
                Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show()
                loadTodos()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al agregar todo", e)
                Toast.makeText(this, "Error al agregar tarea: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateTodoCompleted(todo: Todo, isCompleted: Boolean) {
        if (todo.id.isEmpty()) return

        Log.d(TAG, "Actualizando todo ${todo.id}: completed=$isCompleted")

        db.collection("todos")
            .document(todo.id)
            .update("completed", isCompleted)
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al actualizar todo", e)
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTodos() {
        val userId = auth.currentUser?.uid ?: return
        Log.d(TAG, "Cargando todos para usuario: $userId")

        db.collection("todos")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshots ->
                val todoList = snapshots.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Todo::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear documento ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "Todos cargados: ${todoList.size} items")
                todoAdapter.updateTodos(todoList)

                if (todoList.isEmpty()) {
                    Toast.makeText(this, "No hay tareas. ¡Agrega una!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al cargar todos", e)
                Toast.makeText(this, "Error al cargar tareas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /
    private fun editarTodo(todo: Todo) {
        if (todo.id.isEmpty()) {
            Toast.makeText(this, "No se puede editar: ID vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val editText = EditText(this)
        editText.setText(todo.text)

        AlertDialog.Builder(this)
            .setTitle("Editar tarea")
            .setView(editText)
            .setPositiveButton("Siguiente") { _, _ ->
                val nuevoTexto = editText.text.toString().trim()
                if (nuevoTexto.isEmpty()) {
                    Toast.makeText(this, "El texto no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                mostrarDatePickerParaTodo(todo, nuevoTexto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDatePickerParaTodo(todo: Todo, nuevoTexto: String) {
        val calendar = Calendar.getInstance()


        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            val fechaSeleccionada = String.format("%02d/%02d/%04d", d, m + 1, y)

            db.collection("todos")
                .document(todo.id)
                .update(
                    mapOf(
                        "text" to nuevoTexto,
                        "finalDate" to fechaSeleccionada
                    )
                )
                .addOnSuccessListener {
                    loadTodos()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al editar tarea", e)
                    Toast.makeText(this, "Error al editar: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }, year, month, day).show()
    }

    private fun deshabilitarTodo(todo: Todo) {
        if (todo.id.isEmpty()) {
            Toast.makeText(this, "No se puede deshabilitar: ID vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoEstado = !todo.enabled  // si está habilitado, lo deshabilita; si está deshabilitado, lo habilita

        db.collection("todos")
            .document(todo.id)
            .update("enabled", nuevoEstado)
            .addOnSuccessListener { loadTodos() }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al cambiar estado enabled", e)
                Toast.makeText(this, "Error al deshabilitar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarTodo(todo: Todo) {
        if (todo.id.isEmpty()) {
            Toast.makeText(this, "No se puede eliminar: ID vacío", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar tarea")
            .setMessage("¿Seguro que quieres eliminar esta tarea?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("todos")
                    .document(todo.id)
                    .delete()
                    .addOnSuccessListener { loadTodos() }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al eliminar tarea", e)
                        Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun signOut() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
