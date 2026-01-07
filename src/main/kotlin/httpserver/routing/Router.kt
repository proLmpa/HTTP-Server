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
        routes += Route(method, path, handler)
    }

    fun route(request: HttpRequest): HttpResponse {
        for (route in routes) {
            if (route.matches(request))
                return try {
                    route.handler(request)
                } catch (_: Exception) {
                    internalServerError()
                }
        }
        return notFound()
    }
}