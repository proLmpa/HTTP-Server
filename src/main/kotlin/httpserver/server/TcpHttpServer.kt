package httpserver.server

import httpserver.handler.ImageHandler
import httpserver.handler.TextHandler
import httpserver.handler.TimeHandler
import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.routing.Router
import httpserver.storage.TextStore
import httpserver.util.RequestParser
import httpserver.util.ResponseWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.UUID

class TcpHttpServer(private val port: Int) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val textStore = TextStore()
    private val textHandler = TextHandler(textStore)

    private val router = Router().apply {
        register(HttpMethod.GET, "/time", TimeHandler::handle)
        register(HttpMethod.POST, "/text/{id}", textHandler::create)
        register(HttpMethod.GET, "/text/{id}", textHandler::get)
        register(HttpMethod.GET, "/textall", textHandler::getAll)
        register(HttpMethod.DELETE, "/text/{id}", textHandler::delete)
        register(HttpMethod.GET, "/image", ImageHandler::handle)
    }

    fun start() {
        val serverSocket = ServerSocket(port)
        println("Listening on $port")

        while (true) {
            val socket = serverSocket.accept()

            scope.launch {
                handleConnection(socket)
            }
        }
    }

    private fun handleConnection(socket: Socket) {
        val connectionId = UUID.randomUUID().toString().substring(0, 8)

        socket.soTimeout = 10_000 // 10 seconds

        socket.use { client ->
            val input = client.getInputStream()
            val output = client.getOutputStream()

            println("[CONN $connectionId] opened")

            while (true) {
                val request = try {
                    RequestParser.parse(input) ?: break
                } catch (_: SocketTimeoutException) {
                    break
                }
                val response = router.route(request)
                val keepAlive = shouldKeepAlive(request)

                println("[CONN $connectionId] ${request.method} ${request.path}")

                val finalResponse =
                    if (keepAlive)
                        response.withHeader("Connection", "keep-alive")
                    else
                        response.withHeader("Connection", "close")

                ResponseWriter.write(output, finalResponse)

                if (!keepAlive) break
            }

            println("[CONN $connectionId] closed")
        }
    }

    private fun shouldKeepAlive(request: HttpRequest): Boolean {
        val connection = request.headers["connection"]?.lowercase()

        return if (request.version == "HTTP/1.0") {
            connection == "keep-alive"
        } else {
            connection != "close"
        }
    }
}