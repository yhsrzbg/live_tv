package com.yhsrzbg.live_tv.core.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject

fun JsonObject.array(key: String): List<JsonElement> = (this[key] as? kotlinx.serialization.json.JsonArray).orEmpty()

fun JsonObject.str(key: String): String = (this[key] as? JsonPrimitive)?.content ?: ""

fun JsonObject.int(key: String): Int = str(key).toIntOrNull() ?: 0

fun JsonObject.long(key: String): Long = str(key).toLongOrNull() ?: 0L

fun JsonElement?.asObj(): JsonObject? = this as? JsonObject
