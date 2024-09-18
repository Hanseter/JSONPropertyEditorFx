package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.EnumModel
import com.github.hanseter.json.editor.types.EnumSetModel
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.scene.control.Skin
import javafx.util.StringConverter
import org.controlsfx.control.CheckComboBox
import org.json.JSONArray

class EnumSetControl(
    private val model: EnumSetModel,
    private val context: EditorContext
) : ControlWithProperty<JSONArray?> {
    private val propListener = ChangeListener<JSONArray?> { _, _, new -> propChanged(new) }
    private val controlListener = ListChangeListener<Int> { c ->
        while (c.next()) {
            //mill changes
        }
        //checkbox model is buggy when there are two selected elements and the first one was deselected.
        //even though this looks like a nop, it forces the checkbox model to "reload"
        //should be this bug in controlsfx: https://github.com/controlsfx/controlsfx/issues/1550
        c.list.forEach {  }
        controlChanged(c.list)
    }

    override val property: Property<JSONArray?> = SimpleObjectProperty<JSONArray?>(null).apply {
        addListener(propListener)
    }

    override val control = object : CheckComboBox<String?>() {
        override fun createDefaultSkin(): Skin<*> {
            // controls fx resets the styleclasses when creating the skin. Make sure to preserve them.
            val oldClasses = styleClass.toList()
            val ret = super.createDefaultSkin()
            styleClass.addAll(oldClasses)
            return ret
        }
    }.apply { checkModel.checkedIndices.addListener(controlListener) }

    init {

        val enumDescriptions = EnumModel.getEnumDescriptions(model.schema, model.contentSchema)

        control.items.setAll(
            model.contentSchema.possibleValuesAsList.filterNotNull().map { it.toString() })

//        val cellFactory = Callback<ListView<String?>, ListCell<String?>> {
//            object : ListCell<String?>() {
//
//                private val descLabel = Label().apply {
//                    styleClass.add("enum-desc-label")
//
//                    val descClip = Rectangle().also {
//
//                        it.widthProperty().bind(widthProperty())
//                        it.heightProperty().bind(heightProperty())
//
//                        // if the label is wider than the space available, its layoutX will be negative
//                        // and it will be rendered outside the combobox cell (on the left side)
//                        // with this, we clip it to be always contained within it
//                        it.layoutXProperty().bind(Bindings.max(0, layoutXProperty().negate()))
//                        it.layoutYProperty().bind(layoutYProperty())
//                    }
//
//                    clip = descClip
//                }
//
//                override fun updateItem(item: String?, empty: Boolean) {
//                    super.updateItem(item, empty)
//
//
//
//                    if (item == null || empty) {
//                        text = ""
//                        graphic = null
//                    } else {
//                        text = item
//                        contentDisplay = ContentDisplay.RIGHT
//                        graphic = null
//
//                        enumDescriptions[item]?.let {
//                            descLabel.text = "- $it"
//                            graphic = descLabel
//                        }
//                    }
//                }
//
//            }
//        }
//        control.buttonCell = cellFactory.call(null)
//        control.cellFactory = cellFactory

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
    }

    override fun previewNull(isNull: Boolean) {
        val wasNull = control.title != null
        control.title = if (isNull) TypeControl.NULL_PROMPT else null
        if (wasNull != isNull) {
//            force UI refresh.Unfortunately simply updating title doesn't do the trick :(
            pauseControlListener {
                val checks = control.checkModel.checkedIndices.toIntArray()
                val items = control.items.toList()
                control.items.clear()
                control.items.addAll(items)
                control.checkModel.checkIndices(*checks)
            }
        }
    }

    private fun controlChanged(checkedIndices: List<Int>) {
        property.removeListener(propListener)
        val possibleValues = model.contentSchema.possibleValuesAsList.filterNotNull()
        property.value =
            JSONArray(checkedIndices.mapNotNull { possibleValues.getOrNull(it)?.toString() }
                .distinct())
        property.addListener(propListener)
    }

    private fun propChanged(new: JSONArray?) {
        pauseControlListener {
            control.checkModel.clearChecks()
            val value = (new ?: model.defaultValue)?.toSet()
            if (value == null || model.rawValue == null) return

            control.checkModel.checkIndices(*value.map { control.items.indexOf(it) }
                .filter { it > -1 }.toIntArray())
        }
    }


    private inline fun pauseControlListener(logic: () -> Unit) {
        control.checkModel.checkedIndices.removeListener(controlListener)
        try {
            logic()
        } finally {
            control.checkModel.checkedIndices.addListener(controlListener)
        }
    }

    private fun JSONArray.toSet(): Set<String> =
        (0 until length()).mapNotNullTo(HashSet()) { optString(it, null) }

}