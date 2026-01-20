// Setup.kt
// Archivo de ayuda para establecer la conexión con MongoDB y operaciones sencillas
// Comentarios añadidos en español para facilitar su comprensión

import com.mongodb.MongoException
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import org.bson.BsonInt64
import org.bson.Document
import java.util.*

fun main() {
    // Punto de entrada: iniciamos un contexto coroutine para operaciones IO
    runBlocking {
        // Intenta obtener una conexión a la base de datos usando valores por defecto
        val database = setupConnection()

        // Si la conexión fue exitosa, listamos y borramos colecciones de ejemplo
        if (database != null) {
            listAllCollection(database = database)
            // dropCollection(database = database)
        }
    }
}

suspend fun setupConnection(
    databaseName: String? = null // Parámetro opcional: permite sobrescribir el nombre de la BD
): MongoDatabase? {
    // Cargar .env si existe (no fallar si falta)
    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    // Determina el nombre de la base de datos en este orden:
    // 1) valor explícito pasado como argumento
    // 2) variable en .env: MONGODB_DBNAME
    // 3) variable del entorno del sistema: MONGODB_DBNAME
    val effectiveDbName = databaseName
        ?: dotenv["MONGODB_DBNAME"]
        ?: System.getenv("MONGODB_DBNAME")

    // Host/usuario/contraseña también se resuelven preferentemente desde .env
    val host = dotenv["MONGODB_HOST"] ?: System.getenv("MONGODB_HOST")
    val user = dotenv["MONGODB_USER"] ?: System.getenv("MONGODB_USER")
    val password = dotenv["MONGODB_PASSWORD"] ?: System.getenv("MONGODB_PASSWORD")

    // Construye la cadena de conexión según si hay credenciales disponibles
    val connectString = when {
        user != null && password != null -> "mongodb+srv://$user:$password@$host/?retryWrites=true&w=majority"
        else -> "mongodb+srv://<username>:<password>@cluster0.xxxxxx.mongodb.net/?retryWrites=true&w=majority"
    }

    // Crear cliente y obtener la referencia a la base de datos
    val client = MongoClient.create(connectionString = connectString)
    val database = client.getDatabase(databaseName = effectiveDbName)

    // este return ejecuta un try/catch antes de retornar el objeto database
    return try {
        // Enviar un ping para confirmar conexión correcta
        val command = Document("ping", BsonInt64(1))
        database.runCommand(command)
        println("Pinged!. Estás conectado a MongoDB!")
        database // devolver la instancia de la base de datos
    } catch (me: MongoException) {
        // En caso de error, imprimir y devolver null
        System.err.println(me)
        null
    }
}

suspend fun listAllCollection(database: MongoDatabase) {
    // Obtiene el número de colecciones y las imprime
    val count = database.listCollectionNames().count()
    println("Collection count $count")

    // Itera y muestra los nombres de las colecciones
    print("Collection in this database are ---------------> ")
    database.listCollectionNames().collect { print(" $it") }
    println()
}

suspend fun dropCollection(database: MongoDatabase) {
    // Ejemplo de borrado de una colección (ajusta el nombre real antes de usar)
    database.getCollection<Objects>(collectionName = "restaurants").drop()
}
