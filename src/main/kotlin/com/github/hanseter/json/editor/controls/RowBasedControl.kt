package com.github.hanseter.json.editor.controls

import javafx.scene.control.Control
import javafx.scene.layout.HBox
import javafx.scene.control.Label
import org.everit.json.schema.Schema
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import org.json.JSONObject
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.Property
import org.json.JSONArray
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.StringProperty
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.ReadOnlyBooleanProperty

abstract class RowBasedControl<S : Schema, TYPE : Any, C : Control>(
	override val schema: SchemaWrapper<S>,
	protected val control: C,
	protected val value: Property<TYPE?>,
	protected val defaultValue: TYPE?
) : TypeControl, ChangeListener<TYPE?> {
	private val attributeNameLabel = createLabel(schema)
	override val node = HBox(attributeNameLabel, createSpacer(), control)
	protected var bound: BindableJsonType? = null
	override val valid = SimpleBooleanProperty(true)

	init {
		node.minHeight = MIN_ROW_HEIGHT
		control.setDisable(schema.readOnly)
		value.setValue(defaultValue)
		value.addListener(this)
	}

	constructor(
		schema: SchemaWrapper<S>,
		control: C,
		propExtractror: (C) -> Property<TYPE?>,
		defaultValue: TYPE?
	) : this(
		schema,
		control,
		propExtractror(control),
		defaultValue
	)

	override fun matchesFilter(filterString: String, parentAttributeDisplayName: String): Boolean =
		attributeMatchesFilter(
			filterString,
			attributeNameLabel.text,
			createQualifiedAttributeName(parentAttributeDisplayName, attributeNameLabel.text)
		)

	override fun changed(observable: ObservableValue<out TYPE?>, oldValue: TYPE?, newValue: TYPE?) {
		bound?.setValue(schema, newValue)
	}

	override fun bindTo(type: BindableJsonType) {
		bound = null
		var newVal = type.getValue(schema) as? TYPE
		if (newVal == null) {
			newVal = defaultValue
		}
		if (newVal != this.value.getValue()) {
			this.value.setValue(newVal)
			valueNewlyBound()
		}
		bound = type
	}

	open protected fun valueNewlyBound() {}

	companion object {
		const val MIN_ROW_HEIGHT = 30.0
		private fun createLabel(schema: SchemaWrapper<*>): Label {
			val ret = Label(schema.title)
			val description = schema.schema.getDescription()
			if (description != null) {
				ret.setTooltip(Tooltip(description))
			}
			return ret
		}

		private fun createSpacer(): HBox {
			val ret = HBox()
			HBox.setHgrow(ret, Priority.ALWAYS)
			return ret
		}
	}
}