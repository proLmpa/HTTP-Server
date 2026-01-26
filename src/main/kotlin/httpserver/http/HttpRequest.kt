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
    val body: ByteArray?,
    val pathParams: Map<String, String> = emptyMap()
)