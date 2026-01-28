package httpserver.handler

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import httpserver.storage.TextRepository
import kotlin.text.toByteArray

class TextHandler(
    private val textRepository: TextRepository
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

        textRepository.insert(textId, text)

        return HttpResponse.created("/text/$textId")
    }

    fun get(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed()
        }

        val textId = request.pathParams["id"]
            ?: return HttpResponse.badRequest("id path parameter is required")

        val text = textRepository.get(textId)
            ?: return HttpResponse.notFound()

        return HttpResponse.okText(
            text.toByteArray(Charsets.UTF_8)
        )
    }

    fun getAll(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed()
        }

        val messages = textRepository.getAll()

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

        val existed = textRepository.delete(textId)
        if (!existed) {
            return HttpResponse.notFound()
        }

        return HttpResponse.noContent()
    }

}