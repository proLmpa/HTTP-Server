package httpserver.storage

class TextRepository(private val database: Database) {
    fun insert(id: String, value: String) {
        database.withConnection { connection ->
            connection.prepareStatement(
                """
                    insert into text (id, value)
                    values (?, ?)
                    on conflict (id) do update set value = excluded.value
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, id)
                statement.setString(2, value)
                statement.executeUpdate()
            }
        }
    }

    fun get(id: String): String? {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "select value from text where id = ?"
            ).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { result ->
                    if (!result.next()) return@withConnection null
                    result.getString("value")
                }
            }
        }
    }

    fun delete(id: String): Boolean {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "delete from text where id = ?"
            ).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate() > 0
            }
        }
    }

    fun getAll() : Map<String, String> {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "select id, value from text order by id"
            ).use { statement ->
                statement.executeQuery().use { result ->
                    val values = linkedMapOf<String, String>()
                    while (result.next()) {
                        values[result.getString("id")] = result.getString("value")
                    }
                    values
                }
            }
        }
    }
}