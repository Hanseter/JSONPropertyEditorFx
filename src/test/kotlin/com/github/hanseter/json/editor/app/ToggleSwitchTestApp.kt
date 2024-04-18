package com.github.hanseter.json.editor.app

import com.github.hanseter.json.editor.ui.skins.ToggleSwitchSkin
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

fun main() {
    Application.launch(ToggleSwitchTestApp::class.java)
}

class ToggleSwitchTestApp : Application() {


    override fun start(primaryStage: Stage) {

        val switch1 = CheckBox().apply {
            text = "Switch 1"
            skin = ToggleSwitchSkin(this)
        }
        val switch2 = CheckBox().apply {
            id = "switch2"
            text = "Switch 2"
            skin = ToggleSwitchSkin(this)
        }
        val switch3 = CheckBox().apply {
            text = "Switch 3"
            isSelected = true
            skin = ToggleSwitchSkin(this)
        }



        switch1.selectedProperty().addListener { _, _, new ->
            switch2.isSelected = new
            switch3.text = "switch 1 selected: ${switch1.isSelected}"
        }

        val root = VBox(16.0,
            switch1, switch2, switch3
        ).apply {
            padding = Insets(32.0)
        }

        primaryStage.apply {
            title = "Toggle Switches Galore"
            scene = Scene(root)
        }

        primaryStage.show()
    }

}