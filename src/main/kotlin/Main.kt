package org.YairElazar

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class Main : Application() {
    override fun start(primaryStage: Stage) {
        val root = StackPane(Label("שלום DoIt!"))
        val scene = Scene(root, 400.0, 300.0)
        primaryStage.title = "DoIt App"
        primaryStage.scene = scene
        primaryStage.show()
    }
}

fun main() {
    Application.launch(Main::class.java)
}
