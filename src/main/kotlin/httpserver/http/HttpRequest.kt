package httpserver.http

/*
# HTTP Request 구조 (RFC 기준)
POST /text/abc HTTP/1.1\r\n
Host: localhost:8080\r\n
Content-Type: text/plain\r\n
Content-Length: 11\r\n
\r\n
hello world
 */

data class HttpRequest (
    val method: HttpMethod,
    val path: String,
    val version: String,
    val headers: Map<String, String>,
    val body: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpRequest

        if (method != other.method) return false
        if (path != other.path) return false
        if (version != other.version) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }
}