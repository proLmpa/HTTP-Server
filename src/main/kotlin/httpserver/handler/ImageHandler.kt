package httpserver.handler

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import httpserver.http.HttpStatus
import java.io.File

object ImageHandler {

    private const val IMAGE_PATH = "resources/image.jpg"

    fun handle(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed()
        }

        val file = File(IMAGE_PATH)
        if (!file.exists() || !file.isFile) {
            return HttpResponse.notFound()
        }

        val bytes = try {
            file.readBytes()
        } catch (_: Exception) {
            return HttpResponse.internalServerError()
        }

        return HttpResponse(
            status = HttpStatus.OK,
            headers = mapOf(
                "Content-Type" to "image/jpeg",
                "Content-Length" to bytes.size.toString(),
                "Content-Disposition" to "attachment; filename=\"image.jpg\""
            ) as MutableMap<String, String>,
            body = bytes
        )
    }
}