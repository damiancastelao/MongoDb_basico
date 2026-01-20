// Models.kt
// Define las estructuras de datos (Data Classes) que representan los documentos en MongoDB
// Se usan anotaciones Bson para mapear campos especiales como _id

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.LocalDateTime

// Representa un restaurante en la base de datos
data class Restaurant(
    @BsonId // Marca este campo como el identificador único (_id) de MongoDB
    val id: ObjectId,
    val address: Address,
    val borough: String,
    val cuisine: String,
    val grades: List<Grade>,
    val name: String,
    @BsonProperty("restaurant_id") // Mapea la propiedad 'restaurantId' al campo 'restaurant_id' en la BD
    val restaurantId: String
)

// Sub-documento para la dirección
data class Address(
    val building: String,
    val street: String,
    val zipcode: String,
    val coord: List<Double>
)

// Sub-documento para las calificaciones
data class Grade(
    val date: LocalDateTime,
    val grade: String,
    val score: Int
)
