Readme-flow.md

Objetivo

Explicar, paso a paso, cómo crear una función en Kotlin usando el driver de corutinas de MongoDB para usar la funcionalidad "watch" (Change Streams) sobre la colección "restaurants".

Prerequisitos

- Proyecto Kotlin con Gradle (como el repositorio actual).
- Dependencia del driver de Kotlin/coroutines de MongoDB añadida al build.gradle.kts del proyecto. Consulta la documentación oficial de MongoDB para obtener la coordenada exacta de la dependencia (usa la versión estable más reciente).
- Archivo `.env` o variables de entorno configuradas con la conexión a MongoDB (MONGODB_HOST, MONGODB_USER, MONGODB_PASSWORD o una URI completa). También puedes definir `MONGODB_DBNAME` para el nombre de la base de datos.

Resumen de pasos

1) Preparar el entorno (.env / variables)
2) Añadir (o reutilizar) la función `setupConnection` que devuelve `MongoDatabase` (ya existe en `Setup.kt`).
3) Implementar la función `watchRestaurants(database: MongoDatabase)` que usa `collection.watch(...)` y procesa los eventos como un Flow.
4) Llamar a `watchRestaurants` desde `main` dentro de `runBlocking`.
5) Opciones útiles (pipeline, resume token, fullDocument).

Ejemplo: `.env` mínimo

```text
# Ejemplo .env
MONGODB_HOST=cluster0.xxxxxx.mongodb.net
MONGODB_USER=myUser
MONGODB_PASSWORD=myPassword
MONGODB_DBNAME=simondice
```

Código de ejemplo (paso a paso)

1) Imports y modelos

- Reutiliza tu archivo `Models.kt` que ya contiene `Restaurant`, `Address`, `Grade`, etc.
- Añade los imports necesarios en el archivo que crea la función watch:

```kotlin
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
```

2) Implementar `watchRestaurants`

- Explicación: la función se conecta a la colección `restaurants` (con el tipo `Restaurant`) y llama a `watch()` que devuelve un Flow de eventos. Con `collect {}` consumes los eventos en tiempo real.

Ejemplo:

```kotlin
suspend fun watchRestaurants(database: MongoDatabase) {
    // Obtener la colección tipada
    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")

    // Pipeline vacío -> recibir todos los cambios
    val pipeline: List<Bson> = emptyList()

    println("Iniciando watch en colección 'restaurants'...")

    // watch<T>() devuelve un Flow de ChangeStreamDocument<T>
    collection.watch<Restaurant>(pipeline = pipeline).collect { change: ChangeStreamDocument<Restaurant> ->
        // operationType puede ser INSERT, UPDATE, REPLACE, DELETE, etc.
        println("Cambio detectado: tipo=${change.operationType}")

        // fullDocument puede ser null para algunas operaciones; manejarlo
        val doc = change.fullDocument
        if (doc != null) {
            println("Documento completo: $doc")
        } else {
            println("Sin fullDocument (por ejemplo, delete o fullDocument no solicitado)")
        }

        // Si necesitas el resume token para reiniciar desde aquí
        val token = change.clusterTime
        // ...guardar token si planificas reanudar más tarde
    }
}
```

3) Llamar desde `main` (ejecutar el watcher)

```kotlin
fun main() = runBlocking {
    // Reusar setupConnection() de Setup.kt; si no existe, crea una función similar
    val database = setupConnection() ?: return@runBlocking

    // Llamada a la función que mantiene un stream en ejecución
    watchRestaurants(database)
}
```

Notas y buenas prácticas

- fullDocument: si quieres que `fullDocument` incluya el documento completo en actualizaciones, configura las opciones de Change Stream para solicitar `fullDocument = UpdateLookup` (consulta la documentación del driver para la manera exacta de pasar esta opción desde la API de Kotlin/coroutines).

- Pipeline: puedes filtrar por tipos de operación o por campos concretos. Ejemplo (pseudo-BSON):
  - Filtrar sólo inserts: List<Bson> con Document("$match", Document("operationType", "insert"))

- Resume tokens: captura y persiste el resume token si quieres poder reanudar el stream después de reinicios.

- Robustez: maneja excepciones, reconexiones y `client.close()` cuando termines. Change Streams son conexiones de larga duración; prepara reconexiones automáticas.

- Pruebas locales: para probar, inserta/actualiza documentos en la colección `restaurants` (por ejemplo desde la consola de MongoDB o desde otra función en el proyecto) y observa la salida del watcher.

Posibles errores y soluciones rápidas

- No aparecen eventos: verifica que la colección y la base de datos sean correctas y que la cuenta de usuario tenga permisos para watch (normalmente requiere permisos de lectura en la colección y privilegios para changeStream en la base de datos/cluster).

- fullDocument es null en updates: configura `fullDocument = UpdateLookup` en las opciones del change stream.

- Timeouts o desconexiones: implementa reintentos con backoff y usa resume tokens para reanudar.

Pasos siguientes (opcional)

- Añadir un ejemplo completo en `src/main/kotlin/Watch.kt` con la función `watchRestaurants` y su `main` para ejecución directa.
- Añadir tests que inserten datos y verifiquen que el watcher los recibe (más avanzado; requiere un entorno MongoDB de prueba).

Si quieres, puedo crear el archivo `src/main/kotlin/Watch.kt` con el ejemplo completo y un `main` listo para ejecutar, y/o añadir la instrucción exacta de dependencia para el `build.gradle.kts` si me das permiso para añadir la coordenada de la dependencia o me permites buscar la versión oficial en la web.
