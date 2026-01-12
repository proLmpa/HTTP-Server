package httpserver.handler

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import httpserver.storage.TextStore
import kotlin.text.toByteArray

class TextHandler(
    private val textStore: TextStore
) {
    fun create(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.POST) {
            return HttpResponse.methodNotAllowed()
        }

        val textId = request.pathParams["id"]
            ?: return HttpResponse.badRequest("id path parameter is required")

        val body = request.body
            ?: return HttpResponse.badRequest("request body is required")

        val text = body.toString(Charsets.UTF_8)

        textStore.put(textId, text)

        return HttpResponse.created("/text/$textId")
    }

    fun get(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed()
        }

        val textId = request.pathParams["id"]
            ?: return HttpResponse.badRequest("id path parameter is required")

        val text = textStore.get(textId)
            ?: return HttpResponse.notFound()

        return HttpResponse.okText(
            text.toByteArray(Charsets.UTF_8)
        )
    }

    fun getAll(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed()
        }

        val messages = textStore.getAll()

        val json = messages.entries.joinToString(
            prefix = "{",
            postfix = "}",
        ) { (key, value) ->
            """"$key": "$value""""
        }

        return HttpResponse.okJson(json.toByteArray(Charsets.UTF_8))
    }

    fun delete(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.DELETE) {
            return HttpResponse.methodNotAllowed()
        }

        val textId = request.pathParams["id"]
            ?: return HttpResponse.badRequest("id path parameter is required")

        val existed = textStore.delete(textId)
        if (!existed) {
            return HttpResponse.notFound()
        }

        return HttpResponse.noContent()
    }

}