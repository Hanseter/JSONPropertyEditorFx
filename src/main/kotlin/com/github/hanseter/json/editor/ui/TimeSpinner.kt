package com.github.hanseter.json.editor.ui

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TextFormatter
import javafx.scene.input.InputEvent
import javafx.util.StringConverter


class TimeSpinner constructor(time: String = "00:00:00") : Spinner<String?>() {
    // Property containing the current editing mode:
    private var mode: Mode = Mode.HOURS

    // Mode represents the unit that is currently being edited.
    // For convenience expose methods for incrementing and decrementing that
    // unit, and for selecting the appropriate portion in a spinner's editor
    enum class Mode {
        HOURS {
            override fun increment(time: String?, steps: Int): String? {
                if (time == null) return null
                val hours = (time.take(2).toInt() + steps) % 24
                return "%02d%s".format(hours, time.drop(2))
            }

            override fun select(spinner: TimeSpinner) {
                spinner.editor.selectRange(0, 2)
            }
        },
        MINUTES {
            override fun increment(time: String?, steps: Int): String? {
                if (time == null) return null
                val minutes = (time.substring(3, 5).toInt() + steps) % 60
                return "%s%02d%s".format(time.take(3), minutes, time.drop(5))
            }

            override fun select(spinner: TimeSpinner) {
                spinner.editor.selectRange(3, 5)
            }
        },
        SECONDS {
            override fun increment(time: String?, steps: Int): String? {
                if (time == null) return null
                val seconds = (time.substring(6, 8).toInt() + steps) % 60
                return "%s%02d".format(time.take(6), seconds)
            }

            override fun select(spinner: TimeSpinner) {
                spinner.editor.selectRange(6, 8)
            }
        };

        abstract fun increment(time: String?, steps: Int): String?
        abstract fun select(spinner: TimeSpinner)
        fun decrement(time: String?, steps: Int): String? {
            return increment(time, -steps)
        }
    }

    private fun shiftTimeStringToLeft(time: String): String {
        val sb = StringBuilder()
        time.filter { it.isDigit() }.takeLast(6).forEach {
            sb.append(it)
        }
        while (sb.length < 6) {
            sb.append('0')
        }
        sb.insert(4, ':')
        sb.insert(2, ':')
        return sb.toString()
    }

    private fun handleAdded(c: TextFormatter.Change): TextFormatter.Change {
        c.text = shiftTimeStringToLeft(c.controlNewText)
        c.setRange(0, 8)
        if (c.caretPosition != 0) {
            c.caretPosition = c.caretPosition - 1
            c.anchor = c.anchor - 1
        }
        return c
    }

    private fun handleDeletion(c: TextFormatter.Change): TextFormatter.Change {
        c.text = (c.rangeStart until c.rangeEnd).joinToString("") { "0" }
        return handleAdded(c)
    }

    init {
        isEditable = true
        editor.text = time
        editor.textFormatter = TextFormatter<String> { c: TextFormatter.Change ->
            when {
                c.isReplaced -> if (TIME_REGEX.matches(c.controlNewText)) c else null
                c.isAdded -> handleAdded(c)
                c.isDeleted -> handleDeletion(c)
                else -> c
            }
        }


        val valueFactory: SpinnerValueFactory<String?> = object : SpinnerValueFactory<String?>() {
            override fun decrement(steps: Int) {
                value = mode.decrement(value, steps)
                mode.select(this@TimeSpinner)
            }

            override fun increment(steps: Int) {
                value = mode.increment(value, steps)
                mode.select(this@TimeSpinner)
            }
        }
        valueFactory.converter = object : StringConverter<String?>() {
            override fun toString(`object`: String?) = `object`
            override fun fromString(string: String?): String? = string
        }
        valueFactory.value = time
        setValueFactory(valueFactory)

        // Update the mode when the user interacts with the editor.
        // This is a bit of a hack, e.g. calling spinner.getEditor().positionCaret()
        // could result in incorrect state. Directly observing the caretPosition
        // didn't work well though; getting that to work properly might be
        // a better approach in the long run.
        editor.addEventHandler(InputEvent.ANY) { _ ->
            mode = when (editor.caretPosition) {
                in (0..2) -> Mode.HOURS
                in (3..5) -> Mode.MINUTES
                else -> Mode.SECONDS
            }
        }
        editor.textProperty().addListener { _, _, _ ->
            increment(0)
        }

    }
}

val TIME_REGEX = "([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)".toRegex()
