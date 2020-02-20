import db.LocalDB
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val dbFolderPath = LocalDB.databasesDirectory
    if (!Files.exists(Paths.get(dbFolderPath))) {
        Files.createDirectory(Paths.get(dbFolderPath))
    }
    CommandInterface.command(args)
}