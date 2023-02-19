package mx.edu.ubicatec.ponymaps.models.Classes

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mapbox.maps.extension.style.expressions.dsl.generated.switchCase
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoCollection
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.models.ubicacion.Ubicacion
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionProvider
import org.bson.Document

class AtlasConnection (private val context: Context) {
    var dataCard = ArrayList<DataCard>()
    var ubicacionCards = ArrayList<Ubicacion>()

    enum class ConnectionAccion {
        TestConnection, GenericCard, UbicacionCard, HorarioCard
    }

    fun connectionAtlasBD(db: String, collection: String, action: ConnectionAccion){
        Realm.init(context)
        val appID : String = "ponyapp01-yfpqd"
        val app = App(AppConfiguration.Builder(appID).build())

        val credentials: Credentials = Credentials.anonymous()
        app.loginAsync(credentials) {
            if (it.isSuccess) {
                Log.v("Atlas Connection", "Successfully authenticated anonymously.")

                val user: User? = app.currentUser()
                val mongoCl = user?.getMongoClient("mongodb-atlas")!!
                val mongoDB = mongoCl.getDatabase(db)
                val mongoCollection: MongoCollection<Document> = mongoDB.getCollection(collection)

                val resultTask = mongoCollection.find().iterator()
                //Log.d("Real Response", resultTask.toString())
                resultTask.getAsync(){ result ->
                    if(result.isSuccess){
                        val realmRes = result.get()
                        Log.d("Atlas Connection", "Busqueda Exitosa")


                        when (action) {
                            ConnectionAccion.TestConnection -> {}
                            ConnectionAccion.GenericCard ->
                                try {
                                    realmRes.forEach { doc ->
                                        dataCard.add(docToCard(doc, collection))
                                    }
                                    dataCard.forEach { card ->
                                        Log.d("Generic Card", card.getTitle())
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            ConnectionAccion.UbicacionCard ->
                                try {
                                    realmRes.forEach { doc ->
                                        ubicacionCards.add(docToUbicacion(doc))
                                    }
                                    ubicacionCards.forEach { card ->
                                        Log.d("Ubicacion Card", card.nombre)
                                    }
                                    UbicacionProvider.ubicacionesList = ubicacionCards
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            ConnectionAccion.HorarioCard ->
                                Log.d("Atlas Connection", "Action HorarioCard")
                        }
                    } else {
                        Log.e("Atlas Connection", "Error Busqueda")
                        Toast.makeText(context, "Error de Conexion", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("Atlas Connection", "Failed to log in. Error: ${it.error}")
            }
        }
    }

    //Convert Document type to cards for search fragment
    private fun docToCard(doc: Document?, collection: String): DataCard {
        var title = ""
        var detail = ""
        var cat = ""
        var image = R.drawable.ic_launcher_foreground

        if (collection.equals("materias")){
            title = doc?.getString("nombre").toString()
            cat = "Materia"
        }
        if (collection.equals("horarios")){
            title = doc?.getString("materia").toString()
            detail = doc?.getString("area").toString() + ": "
            detail += (doc?.getString("hora_inicio").toString()) + " - "
            detail += (doc?.getString("hora_fin").toString())
            cat = "Horario"
        }
        if (collection.equals("eventos")){
            title = doc?.getString("nombre").toString()
            detail = doc?.getString("descripcion").toString()
            cat = "Evento"
        }
        if (collection.equals("edificios")){
            title = doc?.getString("nombre").toString()
            //disjoinArray(doc?.get("areas").toString())
            detail = doc?.getString("descripcion").toString()
            cat = "Evento"
        }

        return DataCard(title, detail, cat, image)
        //return card
    }

    //Convert Document type to Ubicacion for their fragment
    private fun docToUbicacion(doc: Document?): Ubicacion {
        val areas = ArrayList<String>()
        //disjoinArray(doc?.get("areas").toString())
        return Ubicacion(
            doc?.getString("nombre").toString(),
            doc?.getString("descripcion").toString(),
            disjoinArray(doc?.get("areas").toString()),
            false
        )
    }

    //Convert the json to string array to separated string elements
    private fun disjoinArray(stringAreas: String): ArrayList<String> {
        if(stringAreas == "null") return ArrayList<String>()
        else {
            val filterAreas = ArrayList<String>()
            var cadena = ""
            var filtro = stringAreas.replace("[", " ")
            filtro = filtro.replace("]", "")

            filtro.forEach {
                if (it.equals(',')) {
                    cadena = cadena.removeRange(0, 1)
                    filterAreas.add(cadena)
                    cadena = ""
                } else cadena += it
            }
            if (cadena != "") {
                cadena = cadena.removeRange(0, 1)
                filterAreas.add(cadena)
            }

            /*filterAreas.forEach{
                Log.d("Lista Filtrada", it)
            }*/
            return filterAreas
        }
    }
}