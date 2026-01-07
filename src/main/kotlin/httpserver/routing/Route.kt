package httpserver.routing

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest

data class Route(
    val method: HttpMethod,
    val pathPattern: String,
    val handler: Handler
) {
    fun matches(request: HttpRequest): Boolean {
        // 1. HTTP Method 확인
        if (request.method != method) return false

        //  2. Path Pattern Parts 수 확인
        val patternParts = pathPattern.split("/")
        val pathParts = request.path.split("/")

        if (patternParts.size != pathParts.size) return false

        // 3. Path Pattern 형식 확인
        return patternParts.zip(pathParts).all { (p, a) ->
            p.startsWith("{") && p.endsWith("}") || p == a
        }
    }
}