// Delete.kt
// Muestra cómo eliminar documentos de la base de datos

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import java.util.regex.Pattern

fun main() {
    runBlocking {
        setupConnection()?.let { db: MongoDatabase ->
            deleteRestaurant(db)
            deleteRestaurants(db)
        }
    }

}

// Elimina un solo documento y lo devuelve
suspend fun deleteRestaurant(db: MongoDatabase) {
    val collection = db.getCollection<Restaurant>(collectionName = "restaurants")
    val queryParams = Filters.eq("restaurant_id", "restaurantId")

    // findOneAndDelete encuentra, borra y devuelve el documento borrado
    collection.findOneAndDelete(filter = queryParams).also {
        it?.let {
            println(it)
        }
    }
}

// Elimina múltiples documentos que coincidan con los criterios
suspend fun deleteRestaurants(db: MongoDatabase) {
    val collection = db.getCollection<Restaurant>(collectionName = "restaurants")

    // Usa expresiones regulares para borrar documentos creados en pruebas anteriores
    val queryParams = Filters.or(
        listOf(
            Filters.regex(Restaurant::name.name, Pattern.compile("^Insert")),
            Filters.regex("restaurant_id", Pattern.compile("^restaurant"))
        )
    )

    collection.deleteMany(filter = queryParams).also {
        println("Document deleted : ${it.deletedCount}")
    }
}
