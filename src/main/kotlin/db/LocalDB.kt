package db

import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager
import java.sql.Types

class LocalDB(val database: String) {

    fun createDB() {
        connection().use {
            println("The driver name is ${it.metaData.driverName}.")
            println("A new database has been created!")
        }
    }

    fun getAllTables(): List<String> {
        val tableNames = mutableListOf<String>()
        val sql = ("SELECT name\n"
                + "    FROM sqlite_master\n"
                + "    WHERE type='table' AND\n"
                + "    name NOT LIKE 'sqlite_%';")

        connection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        tableNames.add(rs.getString("name"))
                    }
                }
            }
        }

        return tableNames
    }


    fun createNewTable() {
        val sql = ("CREATE TABLE IF NOT EXISTS warehouses (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    capacity real\n"
                + ");")

        executeStatement(sql)
    }

    fun query(sql: String): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        connection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        for (i in 1 until (rs.metaData.columnCount + 1)) {
                            val columnName = rs.metaData.getColumnName(i)

                            val value = when (rs.metaData.getColumnType(i)) {
                                Types.INTEGER -> {
                                    rs.getInt(columnName).toString()
                                }
                                Types.VARCHAR -> {
                                    rs.getString(columnName).toString()
                                }
                                Types.REAL -> {
                                    rs.getDouble(columnName).toString()
                                }
                                else -> throw IllegalStateException("Type not supported")
                            }

                            if (result.containsKey(columnName)) {
                                result.get(columnName)?.add(value)
                            } else {
                                result.put(columnName, mutableListOf(value))
                            }
                        }
                    }
                }
            }
        }
        return result
    }


    fun executeStatement(sql: String) = connection().use { connection ->
        connection.createStatement().use {
            it.execute(sql)
        }
    }

    private fun connection() =
        DriverManager.getConnection(connectionString(database))

    companion object {
        val databasesDirectory = Paths.get("").toAbsolutePath().toString() + "/db"
        fun connectionString(database: String) = "jdbc:sqlite:$databasesDirectory/$database.db"

        fun getAllDBNames(): List<String> {
            val list = mutableListOf<String>()
            Files.walk(Paths.get(databasesDirectory))
                .filter {
                    it.fileName.toString().contains(".db")
                }
                .forEach {
                    list.add(it.fileName.toString().replace(".db", ""))
                }

            return list
        }
    }
}