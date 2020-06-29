package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.everit.json.schema.Schema

abstract class RowBasedControl<S : Schema, TYPE : Any, C : Control>(
	override val schema: SchemaWrapper<S>,
	protected val control: C,
	protected val value: Property<TYPE?>,
	protected val defaultValue: TYPE?
) : TypeControl, ChangeListener<TYPE?> {
	private val attributeNameLabel = createLabel(schema)
	override val node = HBox(attributeNameLabel, createSpacer(), control)
	private var bound: BindableJsonType? = null
	override val valid = SimpleBooleanProperty(true)

	init {
		node.minHeight = MIN_ROW_HEIGHT
		control.isDisable = schema.readOnly
		value.value = defaultValue
		value.addListener(this)
	}

	constructor(
			schema: SchemaWrapper<S>,
			control: C,
			propExtractor: (C) -> Property<TYPE?>,
			defaultValue: TYPE?
	) : this(
		schema,
		control,
		propExtractor(control),
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

	protected open fun valueNewlyBound() {}

	companion object {
		const val MIN_ROW_HEIGHT = 30.0
		private fun createLabel(schema: SchemaWrapper<*>): Label {
			val ret = Label(schema.title)
			val description = schema.schema.description
			if (description != null) {
				ret.tooltip = Tooltip(description)
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