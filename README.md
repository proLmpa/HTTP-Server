# HTTP Server

 Simple Kotlin HTTP server implemented on top of raw TCP sockets. The server handles
 basic routing and serves text, time, and a bundled image resource.
 
 ## Features
 
 - `GET /time` returns the current time as JSON.
 - `POST /text/{id}` stores a text payload.
 - `GET /text/{id}` retrieves a stored text payload.
 - `GET /textall` lists all stored text payloads.
 - `DELETE /text/{id}` deletes a stored text payload.
 - `GET /image` returns a bundled JPEG image.
 
 ## Requirements
 
 - JDK 17 
 - Gradle (wrapper included)
 
 ## Run the server
 
 ```bash
 ./gradlew run
 ```
 
 The server listens on `localhost:8080`.
 
 ## Example requests
 
 ```bash
 # Current time
 curl http://localhost:8080/time
 
 # Create text (note the Content-Length header when using raw HTTP)
 curl -X POST http://localhost:8080/text/hello \
   -H "Content-Type: text/plain" \
   --data "Hello World"
 
 # Fetch stored text
 curl http://localhost:8080/text/hello
 
 # List all text
 curl http://localhost:8080/textall
 
 # Delete stored text
 curl -X DELETE http://localhost:8080/text/hello
 
 # Download image
 curl http://localhost:8080/image --output image.jpg
 ```
 
 ## Notes
 
 - The server is HTTP-only (no TLS). Use `http://` for local requests.
 - The `/image` endpoint serves `src/main/resources/image.jpg` from the classpath.
