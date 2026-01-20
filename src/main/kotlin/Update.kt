// Update.kt
// Muestra cómo actualizar documentos existentes

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() {

    runBlocking {
        setupConnection()?.let { db: MongoDatabase ->
            updateSingleDocument(db)
            updateMultipleDocuments(db)
        }
    }
}


// Actualiza un solo documento que coincida con el filtro
suspend fun updateSingleDocument(db: MongoDatabase) {
    val collection = db.getCollection<Restaurant>("restaurants")
    val queryParam = Filters.eq("restaurant_id", "restaurantId")
    
    // Establece un nuevo valor aleatorio para 'restaurant_id'
    val updateParams = Updates.set("restaurant_id", Random.nextInt().toString())
    
    collection.updateOne(filter = queryParam, update = updateParams).also {
        println("Total docs matched ${it.matchedCount} and modified ${it.modifiedCount}")
    }
}

// Actualiza múltiples documentos a la vez
suspend fun updateMultipleDocuments(db: MongoDatabase) {
    val collection = db.getCollection<Restaurant>("restaurants")
    // Filtra todos los restaurantes de cocina 'Chinese'
    val queryParam = Filters.eq(Restaurant::cuisine.name, "Chinese")
    
    // Combina múltiples actualizaciones: cambia cocina a 'Indian' y barrio a 'Brooklyn'
    val updateParams = Updates.combine(
        Updates.set(Restaurant::cuisine.name, "Indian"),
        Updates.set(Restaurant::borough.name, "Brooklyn")
    )

    collection.updateMany(filter = queryParam, update = updateParams).also {
        println("Total docs matched ${it.matchedCount} and modified ${it.modifiedCount}")
    }
}
