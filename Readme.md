# Empezando con el Driver de MongoDB para Kotlin
#### Adaptación del artículo original

> Este es un artículo introductorio sobre cómo construir una aplicación en Kotlin usando [MongoDB Atlas](https://www.mongodb.com/atlas/database) y
> el [Driver de MongoDB para Kotlin](https://github.com/mongodb-developer/kotlin-driver-quick-start), la última incorporación a nuestra lista de drivers oficiales.
> Es una aplicación CRUD que cubre los conceptos básicos de cómo usar MongoDB como base de datos, aprovechando los beneficios de Kotlin como
> lenguaje de programación, como data classes, coroutines y flow.
> En este enlace está la documentación completa del driver [Documentación Driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/)

## Prerrequisitos

Este es un artículo de introducción. Por lo tanto, no se necesita mucho como prerrequisito, pero la familiaridad con Kotlin como lenguaje de programación será
útil.

Además, necesitamos una [cuenta de Atlas](https://www.mongodb.com/cloud/atlas/register), que es gratuita para siempre. Crea una cuenta si no tienes una. Esto
proporciona MongoDB como una base de datos en la nube y mucho más. Más adelante en este tutorial, usaremos esta cuenta para crear un nuevo clúster, cargar un conjunto de datos y
finalmente realizar consultas sobre él.

En general, MongoDB es una base de datos de documentos distribuida, multiplataforma y de código abierto que permite crear aplicaciones con esquemas flexibles. En caso de que
no estés familiarizado con ella o desees un resumen rápido, recomiendo explorar
la [serie MongoDB Jumpstart](https://www.youtube.com/watch?v=RGfFpQF0NpE&list=PL4RCxklHWZ9v2lcat4oEVGQhZg6r4IQGV) para familiarizarte con MongoDB y
sus diversos servicios en menos de 10 minutos. O si prefieres leer, puedes seguir
nuestra [guía](https://www.mongodb.com/docs/atlas/getting-started/).

Y por último, para ayudar en nuestras actividades de desarrollo, usaremos [Jetbrains IntelliJ IDEA (Community Edition)](https://www.jetbrains.com/idea/download/),
que tiene soporte predeterminado para el lenguaje Kotlin.

Después de la sincronización inicial de Gradle, nuestro proyecto está listo para ejecutarse. Así que, probémoslo usando el icono de ejecución en la barra de menú, o simplemente presiona CTRL + R en
Mac. Actualmente, nuestro proyecto no hará mucho aparte de imprimir `Hello World!` y los argumentos suministrados, pero el mensaje `BUILD SUCCESSFUL` en la consola de ejecución
es lo que estamos buscando, lo que nos dice que la configuración de nuestro proyecto está completa.

![build success](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/blt97a67a3d4a402196/64879383d40ad08ec16808a9/Screenshot_2023-06-12_at_13.42.38.png)

Ahora, el siguiente paso es agregar el driver de Kotlin a nuestro proyecto, lo que nos permite interactuar
con [MongoDB Atlas](https://www.mongodb.com/atlas/database).

## Agregando el Driver de MongoDB para Kotlin

Agregar el driver al proyecto es simple y directo. Solo actualiza el bloque `dependencies` con la dependencia del driver de Kotlin en el archivo de construcción
— es decir, `build.gradle`.

```groovy
dependencies {
    // Dependencia de corrutinas de Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Dependencia del driver de MongoDB para Kotlin
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")
    
    // Dependencia para leer archivos .env
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}
```

Y ahora, estamos listos para conectarnos con [MongoDB Atlas](https://www.mongodb.com/atlas/database) usando el driver de Kotlin.

## Conectando a la base de datos

Para conectarnos con la base de datos, primero necesitamos la `Connection URI` que se puede encontrar presionando `connect to cluster` en
nuestra [cuenta de Atlas](https://www.mongodb.com/cloud/atlas/register), como se muestra a continuación.

![image](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/blt1d92c6f1c6654b04/648c2ff42429af5fa2f487e4/atlas_connection_copy_string_kotlin.png)

Para más detalles, también puedes consultar nuestra [documentación](https://www.mongodb.com/docs/guides/atlas/connection-string/).

Con la URI de conexión disponible, el siguiente paso es crear un archivo Kotlin. `Setup.kt` es donde escribimos el código para conectarnos
a [MongoDB Atlas](https://www.mongodb.com/atlas/database).

![Setup.kt file](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/bltdc533d1983ce2f87/6488261e8b23a52669052cee/Screenshot_2023-06-13_at_09.17.29.png)

La conexión con nuestra base de datos se puede dividir en dos pasos. Primero, creamos una instancia de MongoClient usando la `Connection URI`.

```kotlin
val connectionString = "mongodb+srv://<username>:<enter your password>@cluster0.sq3aiau.mongodb.net/?retryWrites=true&w=majority"
val client = MongoClient.create(connectionString = connectString)
```

Y segundo, usamos el cliente para conectarnos con la base de datos, `sample_restaurants`, que es un conjunto de datos de muestra para
restaurantes. Un [conjunto de datos de muestra](https://www.mongodb.com/docs/atlas/sample-data/) es una excelente manera de explorar la plataforma y construir una POC más realista
para validar tus ideas. Para aprender cómo poblar tu primera base de datos Atlas con datos de muestra,
[visita la documentación](https://www.mongodb.com/docs/atlas/sample-data/).

```kotlin
val databaseName = "sample_restaurants"
val db: MongoDatabase = client.getDatabase(databaseName = databaseName)
```

Codificar la `connectionString` no es un buen enfoque y puede llevar a riesgos de seguridad o a la incapacidad de proporcionar acceso basado en roles. Para evitar tales problemas
y seguir las mejores prácticas, usaremos variables de entorno. Otros enfoques comunes son el uso de Vault, variables de configuración de compilación,
y variables de entorno de CI/CD.

### Configuración con .env (Recomendado para desarrollo local)

Para facilitar el desarrollo local sin exponer credenciales, hemos configurado el proyecto para usar un archivo `.env`.

1.  Crea un archivo llamado `.env` en la raíz del proyecto (este archivo está ignorado por git).
2.  Añade tus credenciales en el archivo `.env`:

```env
MONGODB_USER=tu_usuario_real
MONGODB_PASSWORD=tu_contraseña_real
# Opcional: MONGODB_URI=mongodb+srv://...
```

El código se encargará de leer estas variables automáticamente. Si estás en un entorno de CI/CD (como GitHub Actions), el código leerá las variables de entorno del sistema si no encuentra el archivo `.env`.

```kotlin
suspend fun setupConnection(
    databaseName: String? = null
): MongoDatabase? {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }
    
    // Intenta leer de .env, si no existe, lee del sistema
    val user = dotenv["MONGODB_USER"] ?: System.getenv("MONGODB_USER")
    val password = dotenv["MONGODB_PASSWORD"] ?: System.getenv("MONGODB_PASSWORD")
    
    // ... lógica de conexión ...
}
```

Con la función `setupConnection` lista, probémosla y consultemos la base de datos para el recuento y nombre de la colección.

```kotlin
suspend fun listAllCollection(database: MongoDatabase) {

    val count = database.listCollectionNames().count()
    println("Collection count $count")

    print("Collection in this database are -----------> ")
    database.listCollectionNames().collect { print(" $it") }
}
```

Al ejecutar ese código, nuestra salida se ve así:

![list collection output](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/blt5a670a8008abba48/648835185953929729a04668/Screenshot_2023-06-13_at_10.21.15.png)

A estas alturas, es posible que hayas notado que estamos usando la palabra clave `suspend` con `listAllCollection()`. `listCollectionNames()` es una función asíncrona
ya que interactúa con la base de datos y, por lo tanto, idealmente se ejecutaría en un hilo diferente. Y dado que el driver de MongoDB para Kotlin
soporta [Coroutines](https://kotlinlang.org/docs/coroutines-guide.html), el
paradigma nativo de [lenguaje asíncrono de Kotlin](https://kotlinlang.org/docs/async-programming.html), podemos beneficiarnos de él usando funciones `suspend`.

De manera similar, para eliminar colecciones, usamos la función `suspend`.

```kotlin
suspend fun dropCollection(database: MongoDatabase) {
    database.getCollection<Objects>(collectionName = "restaurants").drop()
}
```

Con esto completo, estamos listos para comenzar a trabajar en nuestra aplicación CRUD. Así que para empezar, necesitamos crear una `data` class que represente
la información del restaurante que nuestra aplicación guarda en la base de datos.

```kotlin
data class Restaurant(
    @BsonId
    val id: ObjectId,
    val address: Address,
    val borough: String,
    val cuisine: String,
    val grades: List<Grade>,
    val name: String,
    @BsonProperty("restaurant_id")
    val restaurantId: String
)

data class Address(
    val building: String,
    val street: String,
    val zipcode: String,
    val coord: List<Double>
)

data class Grade(
    val date: LocalDateTime,
    val grade: String,
    val score: Int
)
```

En el fragmento de código anterior, usamos dos anotaciones:

1. `@BsonId`, que representa la identidad única o `_id` de un documento.
2. `@BsonProperty`, que crea un alias para las claves en el documento — por ejemplo, `restaurantId` representa `restaurant_id`.

> Nota: Nuestra data class `Restaurant` aquí es una réplica exacta de un documento de restaurante en el conjunto de datos de muestra, pero algunos campos pueden omitirse o marcarse
> como opcionales — por ejemplo, `grades` y `address` — manteniendo la capacidad de realizar operaciones CRUD. Podemos hacerlo, ya que el modelo de documento de MongoDB
> permite un esquema flexible para nuestros datos.

## Crear

Con todo el trabajo pesado hecho (10 líneas de código para conectar), agregar un nuevo documento a la base de datos es realmente simple y se puede hacer con una
línea de código usando `insertOne`. Así que, creemos un nuevo archivo llamado `Create.kt`, que contendrá todas las operaciones de creación.

```kotlin
suspend fun addItem(database: MongoDatabase) {

    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
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

    collection.insertOne(item).also {
        println("Item added with id - ${it.insertedId}")
    }
}
```

Cuando lo ejecutamos, la salida en la consola es:

![insert one](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/blt1d124cbfb185d7d6/648ae0b2359ef0161360df47/Screenshot_2023-06-15_at_10.49.33.png)

> De nuevo, no olvides agregar una variable de entorno nuevamente para este archivo, si tuviste problemas al ejecutarlo.

Si queremos agregar múltiples documentos a la colección, podemos usar `insertMany`, que se recomienda sobre ejecutar `insertOne` en un bucle.

```kotlin
suspend fun addItems(database: MongoDatabase) {
    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
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

    collection.insertMany(newRestaurants).also {
        println("Total items added ${it.insertedIds.size}")
    }
}

```

![Insert many output](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/blt02fc3f33de844c88/648ae1ce2c4f87306c1b12ce/Screenshot_2023-06-15_at_11.02.48.png)

Con estas salidas en la consola, podemos decir que los datos se han agregado con éxito.

¿Pero qué pasa si queremos ver el objeto en la base de datos? Una forma es con una operación de lectura, que haremos en breve o
usar [MongoDB Compass](https://www.mongodb.com/products/compass) para ver la información.

[MongoDB Compass](https://www.mongodb.com/products/compass) es una herramienta GUI interactiva y gratuita para consultar, optimizar y analizar los datos de MongoDB
desde tu sistema. Para empezar, [descarga](https://www.mongodb.com/try/download/shell) la herramienta y usa la `connectionString` para conectarte con la
base de datos.

![MongoDB compass](https://images.contentstack.io/v3/assets/blt72fd049dd230ea55/648ae40e1fb2d38f0e495940/Screenshot_2023-06-15_at_11.12.23.png)

## Leer

Para leer la información de la base de datos, podemos usar el operador `find`. Comencemos leyendo cualquier documento.

```kotlin
val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
collection.find<Restaurant>().limit(1).collect {
    println(it)
}
```

El operador `find` devuelve una lista de resultados, pero como solo estamos interesados en un solo documento, podemos usar el operador `limit` en conjunto
para limitar nuestro conjunto de resultados. En este caso, sería un solo documento.

Si extendemos esto más y queremos leer un documento específico, podemos agregar parámetros de filtro sobre él:

```kotlin
val queryParams = Filters
    .and(
        listOf(
            eq("cuisine", "American"),
            eq("borough", "Queens")
        )
    )
```

O, podemos usar cualquiera de los operadores de nuestra [lista](https://www.mongodb.com/docs/manual/reference/operator/query/). El código final se ve así.

```kotlin
suspend fun readSpecificDocument(database: MongoDatabase) {
    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
    val queryParams = Filters
        .and(
            listOf(
                eq("cuisine", "American"),
                eq("borough", "Queens")
            )
        )


    collection
        .find<Restaurant>(queryParams)
        .limit(2)
        .collect {
            println(it)
        }

}
```

Para la salida, vemos esto:

![read specific doc output](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/bltd837ac1a039ae43f/648ae83f0f2d9b551eed55e2/Screenshot_2023-06-15_at_11.30.20.png)

> No olvides agregar la variable de entorno nuevamente para este archivo, si tuviste problemas al ejecutarlo.

Otro caso de uso práctico que viene con una operación de lectura es cómo agregar paginación a los resultados. Esto se puede hacer con los operadores `limit` y `offset`.

```kotlin
suspend fun readWithPaging(database: MongoDatabase, offset: Int, pageSize: Int) {
    val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
    val queryParams = Filters
        .and(
            listOf(
                eq(Restaurant::cuisine.name, "American"),
                eq(Restaurant::borough.name, "Queens")
            )
        )

    collection
        .find<Restaurant>(queryParams)
        .limit(pageSize)
        .skip(offset)
        .collect {
            println(it)
        }
}
```

Pero con este enfoque, a menudo, el tiempo de respuesta de la consulta aumenta con el valor del `offset`. Para superar esto, podemos beneficiarnos creando un `Index`,
como se muestra a continuación.

```kotlin
val collection = database.getCollection<Restaurant>(collectionName = "restaurants")
val options = IndexOptions().apply {
    this.name("restaurant_id_index")
    this.background(true)
}

collection.createIndex(
    keys = Indexes.ascending("restaurant_id"),
    options = options
)
```

## Actualizar

Ahora, discutamos cómo editar/actualizar un documento existente. De nuevo, creemos rápidamente un nuevo archivo Kotlin, `Update.Kt`.

En general, hay dos formas de actualizar cualquier documento:

* Realizar una operación de **actualización**, que nos permite actualizar campos específicos de los documentos coincidentes sin afectar los otros campos.
* Realizar una operación de **reemplazo** para reemplazar el documento coincidente con el nuevo documento.

Para este ejercicio, usaremos el documento que creamos anteriormente con la operación de creación `{restaurant_id: "restaurantId"}` y actualizaremos
el `restaurant_id` con un valor más realista. Dividamos esto en dos subtareas para mayor claridad.

Primero, usando `Filters`, consultamos para filtrar el documento, similar a la operación de lectura anterior.

```kotlin
val collection = db.getCollection<Restaurant>("restaurants")
val queryParam = Filters.eq("restaurant_id", "restaurantId")
```

Luego, podemos establecer el `restaurant_id` con un valor entero aleatorio usando `Updates`.

```kotlin
val updateParams = Updates.set("restaurant_id", Random.nextInt().toString())
```

Y finalmente, usamos `updateOne` para actualizar el documento en una operación atómica.

```kotlin
collection.updateOne(filter = queryParam, update = updateParams).also {
    println("Total docs modified ${it.matchedCount} and fields modified ${it.modifiedCount}")
}
```

En el ejemplo anterior, ya sabíamos qué documento queríamos actualizar — el restaurante con un id `restauratantId` — pero podría haber
algunos casos de uso donde esa no sea la situación. En tales casos, primero buscaríamos el documento y luego lo actualizaríamos. `findOneAndUpdate` puede ser
útil. Te permite combinar ambos procesos en una operación atómica, desbloqueando un rendimiento adicional.

Otra variación de lo mismo podría ser actualizar múltiples documentos con una sola llamada. `updateMany` es útil para tales casos de uso — por ejemplo, si queremos
actualizar la `cuisine` de todos los restaurantes a tu tipo de cocina favorito y `borough` a Brooklyn.

```kotlin
suspend fun updateMultipleDocuments(db: MongoDatabase) {
    val collection = db.getCollection<Restaurant>("restaurants")
    val queryParam = Filters.eq(Restaurant::cuisine.name, "Chinese")
    val updateParams = Updates.combine(
        Updates.set(Restaurant::cuisine.name, "Indian"),
        Updates.set(Restaurant::borough.name, "Brooklyn")
    )

    collection.updateMany(filter = queryParam, update = updateParams).also {
        println("Total docs matched ${it.matchedCount} and modified ${it.modifiedCount}")
    }
}
```

En estos ejemplos, usamos `set` y `combine` con `Updates`. Pero hay muchos más tipos de operadores de actualización para explorar que nos permiten hacer muchas
operaciones intuitivas, como establecer la fecha actual o la marca de tiempo, aumentar o disminuir el valor del campo, y así sucesivamente. Para aprender más sobre los diferentes
tipos de operadores de actualización que puedes realizar con Kotlin y MongoDB, consulta
nuestra [documentación](https://mongodb.github.io/mongo-java-driver/4.9/apidocs/mongodb-driver-core/com/mongodb/client/model/Updates.html).

## Eliminar

Ahora, exploremos una operación CRUD final: eliminar. Comenzaremos explorando cómo eliminar un solo documento. Para hacer esto,
usaremos `findOneAndDelete` en lugar de `deleteOne`. Como beneficio adicional, esto también devuelve el documento eliminado como salida. En nuestro ejemplo, eliminamos el
restaurante:

```kotlin
val collection = db.getCollection<Restaurant>(collectionName = "restaurants")
val queryParams = Filters.eq("restaurant_id", "restaurantId")

collection.findOneAndDelete(filter = queryParams).also {
    it?.let {
        println(it)
    }
}
```

![delete output](https://images.contentstack.io/v3/assets/blt39790b633ee0d5a7/blta4bb9c39c2356306/6489bf30352ac64eebda33c6/Screenshot_2023-06-14_at_14.21.37.png)

Para eliminar múltiples documentos, podemos usar `deleteMany`. Podemos, por ejemplo, usar esto para eliminar todos los datos que creamos anteriormente con nuestra operación
de creación.

```kotlin
suspend fun deleteRestaurants(db: MongoDatabase) {
    val collection = db.getCollection<Restaurant>(collectionName = "restaurants")

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
```

## Resumen

¡Felicidades! Ahora sabes cómo configurar tu primera aplicación Kotlin con MongoDB y realizar operaciones CRUD. El código fuente completo de la
aplicación se puede encontrar en [GitHub](https://github.com/mongodb-developer/kotlin-driver-quick-start).

Si tienes algún comentario sobre tu experiencia trabajando con el driver de MongoDB para Kotlin, por favor envía un comentario en nuestro
[portal de comentarios](https://feedback.mongodb.com/) de usuarios o contáctame en Twitter: [@codeWithMohit](https://twitter.com/codeWithMohit).