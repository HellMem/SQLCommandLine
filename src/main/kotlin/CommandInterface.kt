import db.LocalDB
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.system.exitProcess

object CommandInterface {

    fun command(args: Array<String>) {
        while (true) {
            try {
                print("Command > ")
                when (readLine()) {
                    "createdb" -> {
                        var dbName: String? = null
                        while (dbName == null) {
                            println("Input your database name")
                            dbName = readLine()
                        }
                        LocalDB.createDB(dbName)
                        System.setProperty("db", dbName)
                    }
                    "tables" -> {
                        val db = System.getProperty("db", "")
                        if (db.isEmpty()) {
                            println("There's no database selected")
                        } else {
                            println("Tables :")
                            LocalDB.getAllTables(db).forEach {
                                println(it)
                            }
                        }
                    }
                    "exit" -> {
                        exitProcess(0)
                    }
                    "command" -> {
                        if (dbSelected()) {
                            print("\nSQL Command > ")
                            val sql = readLine()
                            sql?.let {
                                LocalDB.executeStatement(db, sql)
                            }
                        }
                    }
                    "query" -> {

                    }
                    "use" -> {
                        var dbName: String? = null
                        while (dbName == null) {
                            println("Input your database name")
                            dbName = readLine()
                        }
                        System.setProperty("db", dbName)
                    }
                    "createTable" -> {
                        val db = System.getProperty("db", "")
                        if (db.isEmpty()) {
                            println("There's no database selected")
                        } else {
                            LocalDB.createNewTable(db)
                        }

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun printQueryResult(queryResultMap: Map<String, List<String>>) {
        queryResultMap.forEach { key, _ ->
            print(generateCell(key))
        }
    }

    fun generateCell(value: String): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("| ")
        stringBuilder.append(value)
        for (i in 0..(30 - stringBuilder.length)) {
            stringBuilder.append(" ")
        }
        stringBuilder.append("|")

        return stringBuilder.toString()
    }

    fun dbSelected(): Boolean {
        val db = System.getProperty("db", "")
        if (db.isEmpty()) {
            println("There's no database selected")
            return false
        }
        return true
    }

    private var db = System.getProperty("db", "")

}