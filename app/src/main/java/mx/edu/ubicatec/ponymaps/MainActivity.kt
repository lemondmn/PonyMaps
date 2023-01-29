package mx.edu.ubicatec.ponymaps

import android.os.Bundle
import android.util.Log
import android.view.Window
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoCollection
import mx.edu.ubicatec.ponymaps.databinding.ActivityMainBinding
import mx.edu.ubicatec.ponymaps.models.Classes.DataCard
import org.bson.Document

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    var dataCard = ArrayList<DataCard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //ActionBar Declaration
        setSupportActionBar(binding.toolbar)
        this.supportActionBar?.hide()

        //NavView
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        //ActionBar Setup
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        //DataBase Consume
        connectionAtlasBD("appdata", "materias")
        connectionAtlasBD("appdata", "horarios")
        connectionAtlasBD("appdata", "eventos")
        //connectionAtlasBD("appdata", "espacios")
        //connectionAtlasBD("appdata", "edificios")
        //connectionAtlasBD("appdata", "areas")

    }

    fun connectionAtlasBD(db: String, collection: String){
        Realm.init(this)
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
                        try {
                            var cont = 0
                            realmRes.forEach {doc ->
                                dataCard.add(docToCard(doc, collection))
                            }
                            dataCard.forEach { card ->
                                Log.d("Card", card.getTitle())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Log.e("Atlas Connection", "Error Busqueda")
                    }
                }
            } else {
                Log.e("Atlas Connection", "Failed to log in. Error: ${it.error}")
            }
        }

        //Toast.makeText(this@MainActivity, "Real OK", Toast.LENGTH_SHORT).show()
    }

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

        val card = DataCard(title, detail, cat, image)
        return card
    }
}