package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.PropertiesEditInput
import com.github.hanseter.json.editor.PropertiesEditResult
import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.extensions.EffectiveSchemaInArray
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import javafx.scene.Node
import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONPointer

val arrayActions =
    listOf(AddToArrayAction, RemoveFromArrayAction, MoveArrayItemDownAction, MoveArrayItemUpAction)

object AddToArrayAction : EditorAction {
    override val description: String =
        JsonPropertiesMl.bundle.getString("jsonEditor.actions.addToArray")
    override val priority: Int
        get() = 500
    override val selector: TargetSelector = TargetSelector.AllOf(
        listOf(
            TargetSelector.ReadOnly.invert(),
            TargetSelector { it.supportedType == SupportedType.ComplexType.ArrayType })
    )

    override fun createIcon(size: Int): Node =
        EditorAction.createTextIcon("\uD83D\uDFA3", size) // \uD83D\uDFA3 = 🞣

    override fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult? {
        // Because this calls model.setValue instead of returning an edit result, we need to ensure
        // the internal value does not change before we invoke setValue, otherwise, it would return
        // immediately. Hence a shallow copy.
        val children =
            (model as TypeModel<JSONArray?, SupportedType.ComplexType.ArrayType>).value?.let {
                JSONArray().putAll(it)
            } ?: JSONArray().also {
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
    override val description: String = "Remove this item"
    override val priority: Int
        get() = 540
    override val selector: TargetSelector
        get() = ArrayChildSelector

    override fun createIcon(size: Int): Node = EditorAction.createTextIcon("-", size)

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
    override val description: String = "Move this item one row up"
    override val priority: Int
        get() = 570
    override val selector: TargetSelector
        get() = ArrayChildSelector

    override fun createIcon(size: Int): Node = EditorAction.createTextIcon("↑", size)

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
    override val description: String = "Move this item one row down"
    override val priority: Int
        get() = 590
    override val selector: TargetSelector
        get() = ArrayChildSelector

    override fun createIcon(size: Int): Node = EditorAction.createTextIcon("↓", size)
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
