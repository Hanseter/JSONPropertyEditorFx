package com.github.hanseter.json.editor.base

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.json.JSONObject

class SimilarObjectMatcher(
    private val reference: JSONObject
) : TypeSafeMatcher<JSONObject>(JSONObject::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("Should be similar to")
            .appendValue(reference)
    }

    override fun matchesSafely(item: JSONObject): Boolean {
        return reference.similar(item)
    }


}