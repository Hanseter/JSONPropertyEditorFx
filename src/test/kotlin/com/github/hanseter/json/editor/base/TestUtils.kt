/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.base

import com.sun.javafx.robot.FXRobot
import javafx.scene.input.KeyCode
import org.json.JSONObject
import org.json.JSONTokener
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
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
}