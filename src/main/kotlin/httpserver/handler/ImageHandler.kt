package httpserver.handler

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import httpserver.http.HttpStatus
import httpserver.storage.ImageData
import httpserver.storage.ImageRepository

class ImageHandler(private val imageRepository: ImageRepository) {

    companion object {
        const val DEFAULT_IMAGE_ID = "default"
    }

    fun get(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed()
        }

        val imageId = request.pathParams["id"]
            ?: return HttpResponse.badRequest("id path parameter is required")

        val image = imageRepository.get(imageId)
            ?: return HttpResponse.notFound()

        return HttpResponse(
            status = HttpStatus.OK,
            headers = mapOf(
                "Content-Type" to image.contentType,
                "Content-Length" to image.bytes.size.toString(),
                "Content-Disposition" to "attachment; filename=\"${imageId}.jpg\""
            ),
            body = image.bytes
        )
    }

    fun put(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.PUT) {
            return HttpResponse.methodNotAllowed()
        }

        val imageId = request.pathParams["id"]
            ?: return HttpResponse.badRequest("id path parameter is required")

        val body = request.body
            ?: return HttpResponse.badRequest("request body is required")
        val contentType = request.headers["content-type"] ?: "application/octet-stream"

        val imageData = ImageData(body, contentType)
        imageRepository.upsert(imageId, imageData)

        return HttpResponse.created("/image/$imageId")
    }

    fun delete(request: HttpRequest): HttpResponse {
        if (request.method != HttpMethod.DELETE) {
            return HttpResponse.methodNotAllowed()
        }

        val imageId = request.pathParams["id"]
            ?: return HttpResponse.badRequest("id path parameter is required")

        val existed = imageRepository.delete(imageId)
        if (!existed) {
            return HttpResponse.notFound()
        }

        return HttpResponse.noContent()
    }
}