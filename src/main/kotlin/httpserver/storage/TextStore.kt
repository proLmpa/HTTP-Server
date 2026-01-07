package httpserver.storage

import java.util.concurrent.ConcurrentHashMap

class TextStore {
    private val store = ConcurrentHashMap<String, String>()

    fun put(id: String, value: String) {
        store[id] = value
    }

    fun get(id: String): String? {
        return store[id]
    }

    fun delete(id: String): Boolean {
        return store.remove(id) != null
    }

    fun getAll(): Map<String, String> {
        return store.toMap()
    }
}