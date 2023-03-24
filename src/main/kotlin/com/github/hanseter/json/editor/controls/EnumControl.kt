package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.EnumModel
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.shape.Rectangle
import javafx.util.Callback
import javafx.util.StringConverter
import org.controlsfx.control.SearchableComboBox
import org.json.JSONObject

//TODO this control makes every enum a string, even if it is something else. This needs to be improved.
class EnumControl(private val model: EnumModel) : ControlWithProperty<String?>,
    ChangeListener<String?> {
    override val control = SearchableComboBox<String?>()
    override val property: Property<String?> = SimpleObjectProperty<String?>(null)

    init {

        val enumDescriptions = model.enumDescriptions

        control.items.setAll(
            model.enumSchema.possibleValuesAsList.filterNotNull().map { it.toString() })
        control.selectionModel.selectedIndexProperty().addListener { _, _, new ->
            if (new.toInt() >= 0) {
                property.removeListener(this)
                property.value =
                    model.enumSchema.possibleValuesAsList.filterNotNull()[new.toInt()].toString()
                property.addListener(this)
            }
        }

        val cellFactory = Callback<ListView<String?>, ListCell<String?>> {
            object : ListCell<String?>() {

                private val descLabel = Label().apply {
                    styleClass.add("enum-desc-label")

                    val descClip = Rectangle().also {

                        it.widthProperty().bind(widthProperty())
                        it.heightProperty().bind(heightProperty())

                        // if the label is wider than the space available, its layoutX will be negative
                        // and it will be rendered outside the combobox cell (on the left side)
                        // with this, we clip it to be always contained within it
                        it.layoutXProperty().bind(Bindings.max(0, layoutXProperty().negate()))
                        it.layoutYProperty().bind(layoutYProperty())
                    }

                    clip = descClip
                }

                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)



                    if (item == null || empty) {
                        text = ""
                        graphic = null
                    } else {
                        text = item
                        contentDisplay = ContentDisplay.RIGHT
                        graphic = null

                        enumDescriptions[item]?.let {
                            descLabel.text = "- $it"
                            graphic = descLabel
                        }
                    }
                }

            }
        }
        control.buttonCell = cellFactory.call(null)
        control.cellFactory = cellFactory

        // Set the String converter because it's used by the search field.
        // It isn't used by the cells itself, so the converted value is never visible.
        control.converter = object : StringConverter<String?>() {
            override fun toString(obj: String?): String {
                return enumDescriptions[obj]?.let {
                    "$obj $it"
                } ?: obj.orEmpty()
            }

            override fun fromString(string: String?): String? = null
        }

        property.addListener(this)
    }

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }

    override fun changed(
        observable: ObservableValue<out String?>?,
        oldValue: String?,
        newValue: String?
    ) {
        setSelectedValue(newValue)
    }

    private fun setSelectedValue(value: String?) {
        control.selectionModel.select(
            when {
                control.items.contains(value) -> value
                model.rawValue == JSONObject.NULL -> null
                else -> model.defaultValue
            }
        )
    }

}