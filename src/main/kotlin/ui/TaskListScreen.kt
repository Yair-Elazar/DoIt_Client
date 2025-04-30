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
    private var selectedTaskForEdit: ApiService.Task? = null  // משתנה לאחסון המשימה הנערכת

    fun createScene(authToken: String): Scene {
        token = authToken

        val titleField = TextField().apply { promptText = "כותרת המשימה" }
        val descriptionField = TextField().apply { promptText = "תיאור המשימה" }
        val datePicker = DatePicker().apply { promptText = "תאריך יעד" }
        val categoryBox = ComboBox<String>().apply {
            items.addAll("כללי", "עבודה", "לימודים", "אישי")
            value = "כללי"
        }
        val addButton = Button("➕ הוסף משימה")

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
                            // כאן תקרא לפונקציה לעדכון השרת
                            println("עדכון סטטוס ל-${checkBox.isSelected} עבור ${task.title}")
                            // לדוגמה: ApiService.updateTaskCompletion(token, task.copy(completed = checkBox.isSelected))
                        }
                    }

                    deleteButton.setOnAction {
                        val task = item
                        if (task != null) {
                            // קריאה לפונקציה למחיקת משימה
                            CoroutineScope(Dispatchers.IO).launch {
                                ApiService.deleteTask(token, task.id)  // מחיקת המשימה מהשרת
                                withContext(Dispatchers.Main) {
                                    taskList.remove(task)  // מחיקת המשימה מהרשימה המקומית
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
                            addButton.text = "שמור שינויים"  // שינוי הטקסט של הכפתור להודעות עדכון
                        }
                    }
                }

                override fun updateItem(task: ApiService.Task?, empty: Boolean) {
                    super.updateItem(task, empty)
                    if (empty || task == null) {
                        graphic = null
                    } else {
                        label.text = "${task.title} "
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

            if (!title.isNullOrBlank() && date != null) {
                if (selectedTaskForEdit == null) {
                    // הוספת משימה חדשה
                    val request = ApiService.CreateTaskRequest(title, description, date.toString(), category, false)
                    CoroutineScope(Dispatchers.IO).launch {
                        ApiService.addTask(token, request)
                        reloadTasks()
                        withContext(Dispatchers.Main) {
                            titleField.clear()
                            descriptionField.clear()
                            datePicker.value = null
                            categoryBox.value = "כללי"
                        }
                    }
                } else {
                    // עדכון משימה קיימת
                    val updatedTask = selectedTaskForEdit!!.copy(
                        title = title,
                        description = description,
                        dueDate = date.toString(),
                        category = category
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        ApiService.updateTask(token, updatedTask)
                        reloadTasks()
                        withContext(Dispatchers.Main) {
                            selectedTaskForEdit = null
                            titleField.clear()
                            descriptionField.clear()
                            datePicker.value = null
                            categoryBox.value = "כללי"
                            addButton.text = "➕ הוסף משימה"  // שינוי הטקסט בחזרה
                        }
                    }
                }
            }
        }

        val inputLayout = VBox(10.0, titleField, descriptionField, datePicker, categoryBox, addButton).apply {
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
                println("שגיאה בטעינת משימות: ${e.message}")
            }
        }
    }
}
