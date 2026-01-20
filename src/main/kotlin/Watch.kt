import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson

// Ejemplo simple de watcher para la colección `restaurants`.
// Requiere que `setupConnection()` exista (como en `Setup.kt`) y que exista la clase `Restaurant` en `Models.kt`.

suspend fun watchRestaurants(database: MongoDatabase, pipeline: List<Bson> = emptyList()) {
    // Obtener la colección tipada
    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")

    println("Iniciando watch en colección 'restaurants'...")

    // Colección.watch<T>() devuelve un Flow de ChangeStreamDocument<T>
    collection.watch<Restaurant>(pipeline = pipeline).collect { change: ChangeStreamDocument<Restaurant> ->
        // operationType puede ser INSERT, UPDATE, REPLACE, DELETE, etc.
        println("Cambio detectado: tipo=${change.operationType}")

        // fullDocument puede ser null para algunas operaciones; manejarlo
        val doc = change.fullDocument
        if (doc != null) {
            // Muestra sólo campos relevantes para evitar impresiones gigantes
            println("Documento completo: name=${doc.name} restaurantId=${doc.restaurantId}")
        } else {
            println("Sin fullDocument, updateDescription=${change.updateDescription}")
        }

        // Si planeas usar resume tokens, guárdalo aquí (pseudo-código):
        // val resumeToken = change.resumeToken
        // persistResumeToken(resumeToken)
    }
}

fun main() = runBlocking {
    // Reusar setupConnection() de Setup.kt; si devuelve null, salir
    val database = setupConnection() ?: return@runBlocking

    // Llamada a la función que mantiene un stream en ejecución
    // (bloqueante). Para ejecutar en background, lanza una coroutine.
    watchRestaurants(database)
}
