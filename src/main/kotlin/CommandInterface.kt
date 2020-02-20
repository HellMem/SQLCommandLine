import db.LocalDB
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.system.exitProcess

object CommandInterface {

    private val dbMap = mutableMapOf<String, LocalDB>()
    private var selectedDb
        set(value) {
            System.setProperty("db", value)
        }
        get() = System.getProperty("db", null)

    private fun getDB(): LocalDB? {
        if (!dbMap.containsKey(selectedDb)) {
            val localDB = LocalDB(selectedDb)
            dbMap[selectedDb] = localDB
        }

        return dbMap[selectedDb]
    }

    private fun createAndSelectDB(name: String) {
        val localDB = LocalDB(name)
        localDB.createDB()
        dbMap[name] = localDB
    }

    fun command(args: Array<String>) {
        while (true) {
            try {
                print("Command > ")
                val fullCommand = readLine()
                val splitedCommand = fullCommand?.split(' ')
                val command = splitedCommand?.get(0) ?: fullCommand
                var arg1: String? = null
                if (splitedCommand?.size!! > 1)
                    arg1 = splitedCommand?.get(1)
                when (command) {
                    "createdb" -> {
                        if (arg1 != null && !LocalDB.getAllDBNames().contains(arg1)) {
                            selectedDb = arg1
                            createAndSelectDB(selectedDb)

                        } else if (arg1 == null) {
                            println("Add a name after 'createdb' command")
                        } else if (LocalDB.getAllDBNames().contains(arg1)) {
                            println("There's already a database named $arg1")
                        }
                    }
                    "tables" -> {
                        if (dbSelected()) {
                            getDB()?.getAllTables()?.forEach {
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
                            //val sql = readLine()
                            val sql = "Insert Into warehouses(name, capacity) values('name', 10.0)"
                            sql?.let {
                                getDB()?.executeStatement(sql)
                            }
                            println()
                        }
                    }
                    "query" -> {
                        if (dbSelected()) {
                            print("\nQuery > ")
                            val sql = "select * from warehouses;"//readLine()
                            //val sql = readLine()
                            sql?.let {
                                getDB()?.query(sql)?.let { result ->
                                    if (result.keys.isNotEmpty())
                                        printQueryResult(result)
                                }

                            }
                            println()
                        }
                    }
                    "use" -> {
                        if (arg1 != null && LocalDB.getAllDBNames().contains(arg1)) {
                            selectedDb = arg1
                        } else if (arg1 == null) {
                            println("Add a name after 'use' command")
                        } else if (!LocalDB.getAllDBNames().contains(arg1)) {
                            println("There's no database called $arg1")
                        }
                    }
                    "createTable" -> {
                        if (dbSelected()) {
                            getDB()?.createNewTable()
                        }
                    }
                    "databases" -> {
                        LocalDB.getAllDBNames().forEach {
                            println(it)
                        }
                    }
                    "currentdb"->{
                        println("Current database is $selectedDb")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun printQueryResult(queryResultMap: Map<String, List<String>>) {
        val cellCeiling = "_________________________________".repeat(queryResultMap.keys.size)
        println(cellCeiling)
        print("|")
        val keys = queryResultMap.keys
        keys.forEach { key ->
            print(generateCell(key))
        }

        print("\n$cellCeiling")

        val rowsSize = queryResultMap[keys.firstOrNull()]?.size ?: 0

        for (i in 0 until rowsSize) {
            print("\n|")
            keys.forEach { key ->
                val cell = generateCell(queryResultMap[key]?.get(i) ?: "")
                print(cell)
            }
        }

        println("\n$cellCeiling")
    }

    private const val CELL_SIZE = 30

    fun generateCell(value: String): String {
        val stringBuilder = StringBuilder()

        val blankSpaceSize = CELL_SIZE - value.length
        val blankSpaceLeftSize = blankSpaceSize / 2
        val blankSpaceRightSize = blankSpaceSize - blankSpaceLeftSize

        for (i in 0..blankSpaceLeftSize) {
            stringBuilder.append(" ")
        }
        stringBuilder.append(value)
        for (i in 0..blankSpaceRightSize) {
            stringBuilder.append(" ")
        }
        stringBuilder.append("|")

        return stringBuilder.toString()
    }

    private fun dbSelected(): Boolean {
        if (selectedDb.isNullOrEmpty()) {
            println("There's no database selected")
            return false
        }
        return true
    }


}