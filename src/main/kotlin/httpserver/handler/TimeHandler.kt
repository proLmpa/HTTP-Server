package httpserver.handler

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TimeHandler {

    fun handle(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed()
        }

        val now = ZonedDateTime.now()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val json = """{"time":"$now"}"""
        val body = json.toByteArray(Charsets.UTF_8)

        return HttpResponse.methodExecuted()
    }
}