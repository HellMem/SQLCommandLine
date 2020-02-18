package db

import java.lang.IllegalStateException
import java.nio.file.Paths
import java.sql.DriverManager
import java.sql.Types

object LocalDB {
    private val systemRootDirectory = Paths.get("").toAbsolutePath().toString()

    fun createDB(database: String) {
        connection(database).use {
            println("The driver name is ${it.metaData.driverName}.")
            println("A new database has been created!")
        }
    }

    fun getAllTables(database: String): List<String> {
        val tableNames = mutableListOf<String>()
        val sql = ("SELECT name\n"
                + "    FROM sqlite_master\n"
                + "    WHERE type='table' AND\n"
                + "    name NOT LIKE 'sqlite_%';")

        resultSet(database, sql).use { rs ->
            while (rs.next()) {
                tableNames.add(rs.getString("name"))
            }
        }

        return tableNames
    }

    fun createNewTable(database: String) {
        val sql = ("CREATE TABLE IF NOT EXISTS warehouses (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    capacity real\n"
                + ");")

        executeStatement(database, sql)
    }

    fun query(database: String, sql: String): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        resultSet(database, sql).use { rs ->
            while (rs.next()) {
                for (i in 1 until (rs.metaData.columnCount)) {
                    val columnName = rs.metaData.getColumnName(i)

                    val value = when (rs.metaData.getColumnType(i)) {
                        Types.INTEGER -> {
                            rs.getInt(columnName).toString()
                        }
                        Types.VARCHAR -> {
                            rs.getString(columnName).toString()
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
        return result
    }


    private fun resultSet(database: String, sql: String) = connection(database).use { connection ->
        connection.createStatement().use {
            it.executeQuery(sql)
        }
    }

    fun executeStatement(database: String, sql: String) = connection(database).use { connection ->
        connection.createStatement().use {
            it.execute(sql)
        }
    }

    private fun connection(fileName: String) =
        DriverManager.getConnection("jdbc:sqlite:$systemRootDirectory/sqlite/db/$fileName.db")
}