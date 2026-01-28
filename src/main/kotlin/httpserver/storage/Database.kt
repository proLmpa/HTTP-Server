package httpserver.storage

import java.sql.Connection
import java.sql.DriverManager

data class DbConfig (
    val url: String,
    val user: String,
    val password: String
) {
    companion object {
        fun fromEnv(): DbConfig {
            val url = System.getenv("DATABASE_URL")
                ?: error("DATABASE_URL is not set")

            val user = System.getenv("DATABASE_USER")
                ?: error("DATABASE_USER is not set")

            val password = System.getenv("DATABASE_PASSWORD")
                ?: error("DATABASE_PASSWORD is not set")

            return DbConfig(url, user, password)
        }
    }
}

class Database(private val config: DbConfig) {
    init {
        Class.forName("org.postgresql.Driver")
        initializeSchema()
    }

    fun <T> withConnection(block: (Connection) -> T): T {
        return DriverManager.getConnection(config.url, config.user, config.password).use(block)
    }

    private fun initializeSchema() {
        withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                        create table if not exists text (
                        id text primary key,
                        value text not null
                        )
                        """.trimIndent()
                )
                statement.execute(
                    """
                        create table if not exists images (
                        id text primary key,
                        content_type text not null,
                        data bytea not null
                        )
                        """.trimIndent()
                )
            }
        }
    }
}