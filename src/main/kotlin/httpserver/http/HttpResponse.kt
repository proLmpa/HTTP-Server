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
    val status: Int,
    val statusMessage: String,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val body: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (status != other.status) return false
        if (statusMessage != other.statusMessage) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status
        result = 31 * result + statusMessage.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        fun methodExecuted(): HttpResponse {
            val body = "200 OK".toByteArray(Charsets.UTF_8)

            return HttpResponse(
                status = 200,
                statusMessage = "OK",
                headers = mutableMapOf(
                    "content-type" to "text/plain",
                    "content-length" to body.size.toString()
                ),
                body = body
            )
        }

        fun created(): HttpResponse {
            val body = "201 Created".toByteArray(Charsets.UTF_8)

            return HttpResponse(
                status = 201,
                statusMessage = "Created",
                headers = mutableMapOf(
                    "content-type" to "text/plain",
                    "content-length" to body.size.toString()
                ),
                body = body
            )
        }

        fun methodNotAllowed(): HttpResponse {
            val body = "405 Method Not Allowed".toByteArray(Charsets.UTF_8)

            return HttpResponse(
                status = 405,
                statusMessage = "Method Not Allowed",
                headers = mutableMapOf(
                    "Content-Type" to "text/plain",
                    "Content-Length" to body.size.toString()
                ),
                body = body
            )
        }

        fun notFound(): HttpResponse {
            val body = "404 Not Found".toByteArray()

            return HttpResponse(
                status = 404,
                statusMessage = "Not Found",
                headers = mutableMapOf(
                    "Content-Type" to "text/plain",
                    "Content-Length" to body.size.toString()
                ),
                body = body
            )
        }

        fun internalServerError(): HttpResponse {
            val body = "500 Internal Server Error".toByteArray()

            return HttpResponse(
                status = 500,
                statusMessage = "Internal Server Error",
                headers = mutableMapOf(
                    "Content-Type" to "text/plain",
                    "Content-Length" to body.size.toString()
                ),
                body = body
            )
        }
    }
}