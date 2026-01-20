// Create.kt
// Muestra cómo insertar documentos (uno o varios) en la colección

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import java.time.LocalDateTime
import kotlin.random.Random

fun main() {
    runBlocking {
        // Conecta y ejecuta las operaciones de inserción
        setupConnection()?.let { db: MongoDatabase ->
            addItem(database = db)
            addItems(database = db)
        }
    }
}

// Inserta un único documento de restaurante
suspend fun addItem(database: MongoDatabase) {

    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
    // Crea un objeto Restaurant con datos aleatorios
    val item = Restaurant(
        id = ObjectId(),
        address = Address(
            building = "Building", street = "street", zipcode = "zipcode", coord =
            listOf(Random.nextDouble(), Random.nextDouble())
        ),
        borough = "borough",
        cuisine = "cuisine",
        grades = listOf(
            Grade(
                date = LocalDateTime.now(),
                grade = "A",
                score = Random.nextInt()
            )
        ),
        name = "name",
        restaurantId = "restaurantId"
    )

    // insertOne inserta un solo documento
    collection.insertOne(item).also {
        println("Item added with id - ${it.insertedId}")
    }

}

// Inserta múltiples documentos a la vez
suspend fun addItems(database: MongoDatabase) {
    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
    
    // Toma el primer restaurante existente como plantilla para crear nuevos
    val newRestaurants = collection.find<Restaurant>().first().run {
        listOf(
            this.copy(
                id = ObjectId(), name = "Insert Many Restaurant first", restaurantId = Random
                    .nextInt().toString()
            ),
            this.copy(
                id = ObjectId(), name = "Insert Many Restaurant second", restaurantId = Random
                    .nextInt().toString()
            )
        )
    }

    // insertMany inserta una lista de documentos de forma eficiente
    collection.insertMany(newRestaurants).also {
        println("Total items added ${it.insertedIds.size}")
    }
}
