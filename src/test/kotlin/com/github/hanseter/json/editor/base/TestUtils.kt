package com.github.hanseter.json.editor.base

import atlantafx.base.theme.CupertinoDark
import atlantafx.base.theme.CupertinoLight
import atlantafx.base.theme.Dracula
import atlantafx.base.theme.NordDark
import atlantafx.base.theme.NordLight
import atlantafx.base.theme.PrimerDark
import atlantafx.base.theme.PrimerLight
import atlantafx.base.theme.Theme
import javafx.application.Application
import javafx.application.Application.setUserAgentStylesheet
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.util.StringConverter
import org.controlsfx.control.SearchableComboBox
import org.json.JSONObject
import org.json.JSONTokener
import org.testfx.util.WaitForAsyncUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

object TestUtils {
    fun loadSchema(schemaName: String) =
        JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream(schemaName)))

    fun getAllSchemas(): List<String> {
        val basePath =
            Paths.get(this::class.java.classLoader.getResource("")!!.toURI()) ?: return emptyList()
        return Files.walk(basePath)
            .filter { Files.isRegularFile(it) && it.toFile().extension == "json" }
            .map { basePath.relativize(it).toString() }.collect(Collectors.toList())
    }

    fun createSchemaComboBox() = SearchableComboBox<String>().apply {
        items.addAll(getAllSchemas())
    }

    fun <T> waitForAsyncFx(callback: () -> T): T? {
        var ret: T? = null
        WaitForAsyncUtils.asyncFx { ret = callback() }
        WaitForAsyncUtils.waitForFxEvents()
        return ret
    }

    fun createThemeComboBox(): ComboBox<Theme?> {
        val themeComboBox = ComboBox(
            FXCollections.observableArrayList(
                null,
                PrimerLight(),
                CupertinoLight(),
                NordLight(),
                PrimerDark(),
                CupertinoDark(),
                NordDark(),
                Dracula(),
            )
        ).apply {
            converter = object : StringConverter<Theme?>() {
                override fun toString(theme: Theme?): String {
                    return theme?.name ?: Application.STYLESHEET_MODENA
                }

                override fun fromString(string: String?): Theme? {
                    TODO("Not yet implemented")
                }
            }
            selectionModel.selectedItemProperty().addListener { _, _, new ->
                if (new != null) {
                    setUserAgentStylesheet(new.userAgentStylesheet)
                } else {
                    setUserAgentStylesheet(null)
                }

            }
        }
        return themeComboBox
    }
}