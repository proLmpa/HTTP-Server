package httpserver.storage

data class ImageData (
    val bytes: ByteArray,
    val contentType: String
)

class ImageRepository(private val database: Database) {
    fun get(id: String): ImageData? {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "select data, content_type from images where id = ?"
            ).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { result ->
                    if (!result.next()) return@withConnection null

                    val bytes = result.getBytes("data")
                    val contentType = result.getString("content_type")
                    ImageData(bytes, contentType)
                }
            }
        }
    }

    fun upsert(id: String, imageData: ImageData) {
        database.withConnection { connection ->
            connection.prepareStatement(
                """
                    insert into images (id, content_type, data)
                    values (?, ?, ?)
                    on conflict (id) do update
                    set content_type = excluded.content_type,
                        data = excluded.data
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, id)
                statement.setString(2, imageData.contentType)
                statement.setBytes(3, imageData.bytes)
                statement.executeUpdate()
            }
        }
    }

    fun delete(id: String): Boolean {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "delete from images where id = ?"
            ).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate() > 0
            }
        }
    }
}