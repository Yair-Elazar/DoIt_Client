package org.YairElazar.ui

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import kotlinx.coroutines.*
import org.YairElazar.network.ApiService
import java.time.LocalDate

object TaskListScreen {
    private val taskList: ObservableList<ApiService.Task> = FXCollections.observableArrayList()
    private lateinit var token: String
    private var selectedTaskForEdit: ApiService.Task? = null
    private lateinit var userComboBox: ComboBox<String>


    fun createScene(authToken: String): Scene {
        token = authToken

        val titleField = TextField().apply { promptText = "Task Title" }
        val descriptionField = TextField().apply { promptText = "Task Description" }
        val datePicker = DatePicker().apply { promptText = "Due Date" }
        val categoryBox = ComboBox<String>().apply {
            items.addAll("General", "Work", "Study", "Personal")
            value = "General"
        }
        val addButton = Button("➕ Add Task")

        userComboBox = ComboBox<String>().apply {
            promptText = "Select user to share"
        }
        loadUsers()



        val taskListView = ListView(taskList)
        taskListView.setCellFactory {
            object : ListCell<ApiService.Task>() {
                private val checkBox = CheckBox()
                private val label = Label()
                private val deleteButton = Button("🗑️").apply {
                    style = "-fx-text-fill: red;"
                }
                private val editButton = Button("✏️").apply {
                    style = "-fx-text-fill: blue;"
                }
                private val content: HBox

                init {
                    val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }
                    content = HBox(10.0, checkBox, label, spacer, editButton, deleteButton).apply {
                        alignment = Pos.CENTER_LEFT
                    }

                    checkBox.setOnAction {
                        val task = item
                        if (task != null) {
                            println("Updating status to ${checkBox.isSelected} for ${task.title}")
                            // Example: ApiService.updateTaskCompletion(token, task.copy(completed = checkBox.isSelected))
                        }
                    }

                    deleteButton.setOnAction {
                        val task = item
                        if (task != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                ApiService.deleteTask(token, task.id)
                                withContext(Dispatchers.Main) {
                                    taskList.remove(task)
                                }
                            }
                        }
                    }

                    editButton.setOnAction {
                        val task = item
                        if (task != null) {
                            selectedTaskForEdit = task
                            titleField.text = task.title
                            descriptionField.text = task.description
                            datePicker.value = LocalDate.parse(task.dueDate)
                            categoryBox.value = task.category
                            addButton.text = "💾 Save Changes"
                        }
                    }
                }

                override fun updateItem(task: ApiService.Task?, empty: Boolean) {
                    super.updateItem(task, empty)
                    if (empty || task == null) {
                        graphic = null
                    } else {
                        label.text = task.title
                        checkBox.isSelected = task.completed
                        graphic = content
                    }
                }
            }
        }

        addButton.setOnAction {
            val title = titleField.text
            val description = descriptionField.text
            val date = datePicker.value
            val category = categoryBox.value
            val selectedUsers = listOf(userComboBox.value)

            if (!title.isNullOrBlank() && date != null) {
                if (selectedTaskForEdit == null) {
                    val request = ApiService.CreateTaskRequest(title, description, date.toString(), category, false)
                    CoroutineScope(Dispatchers.IO).launch {
                        ApiService.addTask(token, request)
                        reloadTasks()
                        withContext(Dispatchers.Main) {
                            titleField.clear()
                            descriptionField.clear()
                            datePicker.value = null
                            categoryBox.value = "General"
                        }
                    }
                } else {
                    val updatedTask = selectedTaskForEdit!!.copy(
                        title = title,
                        description = description,
                        dueDate = date.toString(),
                        category = category,
                        users = selectedUsers
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        ApiService.updateTask(token, updatedTask)
                        reloadTasks()
                        withContext(Dispatchers.Main) {
                            selectedTaskForEdit = null
                            titleField.clear()
                            descriptionField.clear()
                            datePicker.value = null
                            categoryBox.value = "General"
                            addButton.text = "➕ Add Task"
                        }
                    }
                }
            }
        }

        val inputLayout = VBox(10.0, titleField, descriptionField, datePicker, categoryBox, userComboBox, addButton).apply {
            padding = Insets(10.0)
        }

        val root = BorderPane().apply {
            top = inputLayout
            center = taskListView
        }

        val scene = Scene(root, 500.0, 600.0)
        reloadTasks()
        return scene
    }

    private fun reloadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = ApiService.getTasks(token)
                withContext(Dispatchers.Main) {
                    taskList.setAll(tasks)
                }
            } catch (e: Exception) {
                println("Error loading tasks: ${e.message}")
            }
        }
    }
    private fun loadUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users = ApiService.getAllUsernames(token) // צריך להוסיף את הפונקציה הזו ב-ApiService כפי שכתבנו קודם
                withContext(Dispatchers.Main) {
                    userComboBox.items.setAll(users)
                }
            } catch (e: Exception) {
                println("Error loading users: ${e.message}")
            }
        }
    }

}
