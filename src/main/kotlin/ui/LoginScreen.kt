package org.YairElazar.ui

import org.YairElazar.network.ApiService
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.*

class LoginScreen : Application() {
    override fun start(primaryStage: Stage) {
        val usernameField = TextField().apply { promptText = "שם משתמש" }
        val passwordField = PasswordField().apply { promptText = "סיסמא" }
        val actionButton = Button("התחבר")
        val switchModeLink = Hyperlink("אין לך משתמש? הירשם כאן")
        val statusLabel = Label()

        var isLoginMode = true

        fun switchMode() {
            isLoginMode = !isLoginMode
            actionButton.text = if (isLoginMode) "התחבר" else "הרשמה"
            switchModeLink.text = if (isLoginMode) "אין לך משתמש? הירשם כאן" else "כבר יש לך משתמש? התחבר"
            statusLabel.text = ""
            usernameField.clear()
            passwordField.clear()
        }

        actionButton.setOnAction {
            val username = usernameField.text.trim()
            val password = passwordField.text.trim()

            // ולידציה בסיסית
            when {
                username.isBlank() || password.isBlank() -> {
                    statusLabel.text = "יש למלא שם משתמש וסיסמה"
                    return@setOnAction
                }
                username.length < 3 -> {
                    statusLabel.text = "שם המשתמש צריך להכיל לפחות 3 תווים"
                    return@setOnAction
                }
                password.length < 4 -> {
                    statusLabel.text = "הסיסמה צריכה להכיל לפחות 4 תווים"
                    return@setOnAction
                }
            }

            // המשך התחברות או הרשמה...
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = if (isLoginMode) {
                        ApiService.login(username, password)
                    } else {
                        ApiService.register(username, password)
                    }

                    withContext(Dispatchers.Main) {
                        val taskListScene = TaskListScreen.createScene(response.token)
                        primaryStage.scene = taskListScene
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusLabel.text = "שגיאה: ${e.message}"
                    }
                }
            }
        }


        switchModeLink.setOnAction { switchMode() }

        val vbox = VBox(10.0, usernameField, passwordField, actionButton, switchModeLink, statusLabel).apply {
            setPrefSize(300.0, 220.0)
            padding = Insets(20.0)
        }

        val scene = Scene(vbox)
        primaryStage.title = "התחברות"
        primaryStage.scene = scene
        primaryStage.show()
    }
}


fun main() {
    Application.launch(LoginScreen::class.java)
}
