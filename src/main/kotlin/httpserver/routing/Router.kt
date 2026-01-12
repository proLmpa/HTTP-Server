package httpserver.routing

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import httpserver.http.HttpResponse.Companion.internalServerError
import httpserver.http.HttpResponse.Companion.notFound

typealias Handler = (HttpRequest) -> HttpResponse

class Router {
    private val routes = mutableListOf<Route>()

    fun register(method: HttpMethod, path: String, handler: Handler) {
        routes += Route(
            method,
            path.trim('/').split("/"),
            handler
        )
    }

    fun route(request: HttpRequest): HttpResponse {
        for (route in routes) {
            val params = route.match(request) ?: continue

            return try {
                route.handler(request.copy(pathParams = params))
            } catch (_: Exception) {
                internalServerError()
            }
        }
        return notFound()
    }
}