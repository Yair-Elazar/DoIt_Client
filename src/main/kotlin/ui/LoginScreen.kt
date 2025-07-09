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
        val usernameField = TextField().apply { promptText = "Username" }
        val passwordField = PasswordField().apply { promptText = "Password" }
        val actionButton = Button("Login")
        val switchModeLink = Hyperlink("Don't have an account? Register here")
        val statusLabel = Label()

        var isLoginMode = true

        fun switchMode() {
            isLoginMode = !isLoginMode
            actionButton.text = if (isLoginMode) "Login" else "Register"
            switchModeLink.text = if (isLoginMode) "Don't have an account? Register here" else "Already have an account? Login"
            statusLabel.text = ""
            usernameField.clear()
            passwordField.clear()
        }

        actionButton.setOnAction {
            val username = usernameField.text.trim()
            val password = passwordField.text.trim()

            // Basic validation
            when {
                username.isBlank() || password.isBlank() -> {
                    statusLabel.text = "Please enter a username and password"
                    return@setOnAction
                }
                username.length < 3 -> {
                    statusLabel.text = "Username must be at least 3 characters"
                    return@setOnAction
                }
                password.length < 4 -> {
                    statusLabel.text = "Password must be at least 4 characters"
                    return@setOnAction
                }
            }

            // Proceed with login or registration
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = if (isLoginMode) {
                        ApiService.login(username, password)
                    } else {
                        ApiService.register(username, password)
                    }

                    withContext(Dispatchers.Main) {
                        val taskListScene = TaskListScreen.createScene(response.token)
                        primaryStage.title = "doit" // Update title after login
                        primaryStage.scene = taskListScene
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusLabel.text = "Error: ${e.message}"
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
        primaryStage.title = "Login"
        primaryStage.scene = scene
        primaryStage.show()
    }
}

fun main() {
    Application.launch(LoginScreen::class.java)
}
