package org.example.test.marketdata.bitget

import org.json.JSONArray
import org.json.JSONObject

enum class BitgetOperation(val wireValue: String) {
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
}

// A single subscribe/unsubscribe request. Owns its own wire encoding so
// the operation type and its payload can never drift apart.
data class BitgetSubscriptionRequest(
    val operation: BitgetOperation,
    val channels: List<BitgetChannel>,
) {
    fun toJsonString(): String {
        val args = JSONArray()
        channels.forEach { args.put(it.toJson()) }
        return JSONObject()
            .put("op", operation.wireValue)
            .put("args", args)
            .toString()
    }
}
