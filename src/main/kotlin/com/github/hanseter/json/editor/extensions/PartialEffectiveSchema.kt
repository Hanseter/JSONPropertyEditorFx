package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.Schema

class PartialEffectiveSchema<T : Schema>(
        override val parent: EffectiveSchema<CombinedSchema>,
        override val baseSchema: T,
) : EffectiveSchema<T> {

    override val defaultValue by parent::defaultValue

    override val title by parent::title

    override val description by parent::description

    override val readOnly by parent::readOnly

    override val pointer by parent::pointer

    override val schemaLocation by parent::schemaLocation

    override val required by parent::required

    override val propertyName by parent::propertyName

    override val propertyOrder by parent::propertyOrder

    override val cssClasses by parent::cssClasses

    override val cssStyle by parent::cssStyle
}