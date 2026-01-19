package httpserver.http

/*
# HTTP Response 구조 (RFC 기준)
HTTP/1.1 200 OK\r\n
Content-Type: application/json\r\n
Content-Length: 27\r\n
Connection: close\r\n
\r\n
{"time":"2025-01-01"}
 */

data class HttpResponse(
    val status: HttpStatus,
    val headers: MutableMap<String, String>,
    val body: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (status != other.status) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }

    fun withHeader(key: String, value: String): HttpResponse {
        return copy(
            headers = (headers + (key to value)) as MutableMap<String, String>
        )
    }

    companion object {

        private fun base(
            status: HttpStatus,
            body: ByteArray? = null,
            headers: Map<String, String> = emptyMap()
        ): HttpResponse {
            val finalHeaders = mutableMapOf<String, String>()

            if (body != null) {
                finalHeaders["Content-Length"] = body.size.toString()
            } else {
                finalHeaders["Content-Length"] = "0"
            }
            finalHeaders.putAll(headers)

            return HttpResponse(status, finalHeaders, body)
        }

        fun okText(body: ByteArray): HttpResponse =
            base(
                status = HttpStatus.OK,
                body = body,
                headers = mapOf(
                    "Content-Type" to "text/plain; charset=utf-8"
                )
            )

        fun okJson(body: ByteArray): HttpResponse =
            base(
                status = HttpStatus.OK,
                body = body,
                headers = mapOf(
                    "Content-Type" to "application/json; charset=utf-8"
                )
            )

        fun created(location: String): HttpResponse =
            base(
                status = HttpStatus.CREATED,
                body = "201 Created".toByteArray(Charsets.UTF_8),
                headers = mapOf(
                    "Location" to location,
                    "Content-Type" to "text/plain; charset=utf-8"
                )
            )

        fun noContent(): HttpResponse = base(HttpStatus.NO_CONTENT)

        fun badRequest(message: String): HttpResponse =
            base(
                status = HttpStatus.BAD_REQUEST,
                body = message.toByteArray(Charsets.UTF_8),
                headers = mapOf(
                    "Content-Type" to "text.plain; charset=utf-8"
                )
            )

        fun notFound(): HttpResponse =
            base(
                status = HttpStatus.NOT_FOUND,
                body = "404 Not Found".toByteArray(Charsets.UTF_8),
                headers = mapOf(
                    "Content-Type" to "text/plain; charset=utf-8"
                )
            )

        fun methodNotAllowed(): HttpResponse =
            base(
                status = HttpStatus.METHOD_NOT_ALLOWED,
                body = "405 Not Allowed".toByteArray(Charsets.UTF_8),
                headers = mapOf(
                    "Content-Type" to "text/plain; charset=utf-8"
                )
            )

        fun internalServerError(): HttpResponse =
            base(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                body = "500 Internal Server Error".toByteArray(Charsets.UTF_8),
                headers = mapOf(
                    "Content-Type" to "text/plain; charset=utf-8"
                )
            )
    }
}