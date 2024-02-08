package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.PropertiesEditInput
import com.github.hanseter.json.editor.PropertiesEditResult
import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.extensions.EffectiveSchemaInArray
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONPointer

val arrayActions =
    listOf(AddToArrayAction, RemoveFromArrayAction, MoveArrayItemDownAction, MoveArrayItemUpAction)

object AddToArrayAction : EditorAction {
    override val text: String = "\uD83D\uDFA3"// \uD83D\uDFA3 = 🞣
    override val description: String = JsonPropertiesMl.bundle.getString("jsonEditor.actions.addToArray")
    override val selector: TargetSelector = TargetSelector.AllOf(
        listOf(
            TargetSelector.ReadOnly.invert(),
            TargetSelector { it.supportedType == SupportedType.ComplexType.ArrayType })
    )

    override fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult? {
        val children = (model as TypeModel<JSONArray?, SupportedType.ComplexType.ArrayType>).value
            ?: JSONArray().also {
                model.value = it
            }
        val childDefaultValue =
            (model.schema.baseSchema as ArraySchema).allItemSchema.defaultValue?.let {
                SchemaNormalizer.deepCopy(it)
            }
        children.put(children.length(), childDefaultValue ?: JSONObject.NULL)
        model.value = children
        // since we just called `model.setValue`, we don't actually need to return anything here
        return null
    }
}

object ArrayChildSelector : TargetSelector {
    override fun matches(model: TypeModel<*, *>): Boolean =
        model.schema.let { it is EffectiveSchemaInArray && !it.parent.readOnly }
}

object RemoveFromArrayAction : EditorAction {
    override val text: String = "-"
    override val description: String = "Remove this item"
    override val selector: TargetSelector
        get() = ArrayChildSelector

    override fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult? {
        val children =
            JSONPointer(model.schema.pointer.dropLast(1)).queryFrom(input.data) as? JSONArray
                ?: return null
        val index = (model.schema as EffectiveSchemaInArray).index
        children.remove(index)
        return PropertiesEditResult(model.bound?.rootType?.getValue() ?: input.data)
    }
}

object MoveArrayItemUpAction : EditorAction {
    override val text: String = "↑"
    override val description: String = "Move this item one row up"
    override val selector: TargetSelector
        get() = ArrayChildSelector

    override fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult? {
        val children =
            JSONPointer(model.schema.pointer.dropLast(1)).queryFrom(input.data) as? JSONArray
                ?: return null
        val index = (model.schema as EffectiveSchemaInArray).index
        if (index == 0) return null
        val tmp = children.get(index - 1)
        children.put(index - 1, children.get(index))
        children.put(index, tmp)
        return PropertiesEditResult(model.bound?.rootType?.getValue() ?: input.data)
    }
}

object MoveArrayItemDownAction : EditorAction {
    override val text: String = "↓"
    override val description: String = "Move this item one row down"
    override val selector: TargetSelector
        get() = ArrayChildSelector

    override fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult? {

        val children =
            JSONPointer(model.schema.pointer.dropLast(1)).queryFrom(input.data) as? JSONArray
                ?: return null
        val index = (model.schema as EffectiveSchemaInArray).index
        if (index >= children.length() - 1) return null
        val tmp = children.get(index + 1)
        children.put(index + 1, children.get(index))
        children.put(index, tmp)
        return PropertiesEditResult(model.bound?.rootType?.getValue() ?: input.data)
    }
}
