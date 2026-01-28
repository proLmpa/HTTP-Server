package httpserver.server

import httpserver.handler.ImageHandler
import httpserver.handler.TextHandler
import httpserver.handler.TimeHandler
import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import httpserver.http.HttpResponse
import httpserver.routing.Router
import httpserver.storage.Database
import httpserver.storage.DbConfig
import httpserver.storage.ImageData
import httpserver.storage.ImageRepository
import httpserver.storage.TextRepository
import httpserver.util.RequestParser
import httpserver.util.ResponseWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

class TcpHttpServer(private val port: Int) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val database = Database(DbConfig.fromEnv())
    private val textRepository = TextRepository(database)
    private val imageRepository = ImageRepository(database)
    private val textHandler = TextHandler(textRepository)
    private val imageHandler = ImageHandler(imageRepository)

    private val router = Router().apply {
        register(HttpMethod.GET, "/time", TimeHandler::handle)
        register(HttpMethod.POST, "/text/{id}", textHandler::create)
        register(HttpMethod.GET, "/text/{id}", textHandler::get)
        register(HttpMethod.GET, "/textall", textHandler::getAll)
        register(HttpMethod.DELETE, "/text/{id}", textHandler::delete)
        register(HttpMethod.GET, "/image/{id}", imageHandler::get)
        register(HttpMethod.PUT, "/image/{id}", imageHandler::put)
        register(HttpMethod.DELETE, "/image/{id}", imageHandler::delete)
    }

    fun start() {
        seedDefaultImage()
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
        socket.soTimeout = 10_000 // 10 seconds

        socket.use { client ->
            val input = client.getInputStream()
            val output = client.getOutputStream()

            while (true) {
                try {
                    val request = try {
                        RequestParser.parse(input) ?: break
                    } catch (_: SocketTimeoutException) {
                        break
                    }
                    val response = router.route(request)
                    val keepAlive = shouldKeepAlive(request)

                    val finalResponse =
                        if (keepAlive)
                            response.withHeader("Connection", "keep-alive")
                        else
                            response.withHeader("Connection", "close")

                    ResponseWriter.write(output, finalResponse)

                    if (!keepAlive) break
                } catch (_: Exception) {
                    ResponseWriter.write(
                        output,
                        HttpResponse.internalServerError()
                    )
                    break
                }
            }
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

    private fun seedDefaultImage() {
        val resource = javaClass.getResourceAsStream("/sample.jpg") ?: return
        val imageData = resource.use { stream ->
            ImageData(stream.readBytes(), "image/jpeg")
        }
        imageRepository.upsert(ImageHandler.DEFAULT_IMAGE_ID, imageData)
    }
}