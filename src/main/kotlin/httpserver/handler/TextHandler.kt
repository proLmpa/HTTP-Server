package httpserver.handler

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import httpserver.storage.TextStore

class TextHandler(
    private val textStore: TextStore
) {
    fun create(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.POST) {
            return HttpResponse.methodNotAllowed()
        }

        val patternParts = request.path.split("/")
        val textId = patternParts[2]

        val body = request.body.toString()

        textStore.put(textId, body)

        return HttpResponse.created()
    }

    fun get(request: HttpRequest): HttpResponse {

    }

    fun getAll(): HttpResponse {

    }

    fun delete(request: HttpRequest): HttpResponse {

    }

}