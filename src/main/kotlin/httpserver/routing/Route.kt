package httpserver.routing

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest

data class Route(
    val method: HttpMethod,
    val segments: List<String>,
    val handler: Handler
) {
    fun match (request: HttpRequest): Map <String, String>? {
        if (request.method != method) return null

        val reqSeg = request.path.trim('/').split("/")
        if (segments.size != reqSeg.size) return null

        val params = mutableMapOf<String, String>()

        segments.zip(reqSeg).forEach { (p, a) ->
            if (p.startsWith("{")) {
                params[p.substring(1, p.length - 1)] = a
            } else if (p != a) return null
        }
        return params
    }
}