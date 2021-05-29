package me.ste.ivremotecontrol.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement

object SerializationUtil {
    fun jsonToTree(element: JsonElement): Any =
        when {
            element.isJsonObject ->
                hashMapOf(
                    *(element.asJsonObject.entrySet().map {
                        it.key to this.jsonToTree(it.value)
                    }).toTypedArray()
                )
            element.isJsonArray ->
                element.asJsonArray.map {
                    this.jsonToTree(it)
                }
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asNumber
                    primitive.isString -> primitive.asString
                    else -> "unknown"
                }
            }
            else -> "unknown"
        }

    fun objectToTree(obj: Any): Any =
        this.jsonToTree(GsonBuilder().create().toJsonTree(obj))
}