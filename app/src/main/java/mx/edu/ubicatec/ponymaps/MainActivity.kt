package mx.edu.ubicatec.ponymaps

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoCollection
import mx.edu.ubicatec.ponymaps.databinding.ActivityMainBinding
import mx.edu.ubicatec.ponymaps.models.Classes.AtlasConnection
import mx.edu.ubicatec.ponymaps.models.Classes.DataCard
import mx.edu.ubicatec.ponymaps.models.ubicacion.Ubicacion
import mx.edu.ubicatec.ponymaps.ui.eventos.EventosFragment
import mx.edu.ubicatec.ponymaps.ui.horarios.HorariosFragment
import mx.edu.ubicatec.ponymaps.ui.ubicaciones.UbicacionesFragment
import org.bson.Document

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var actualFragment: NavDestination
    var connection = AtlasConnection(this)
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
        //connectionAtlasBD("appdata", "materias")
        //connectionAtlasBD("appdata", "horarios")
        //connectionAtlasBD("appdata", "eventos")
        //connectionAtlasBD("appdata", "espacios")
        connection.connectionAtlasBD("appdata", "edificios", AtlasConnection.ConnectionAccion.SaveUbicaciones)
        //connectionAtlasBD("appdata", "areas")

        //Fragment Change Controller
        navController.addOnDestinationChangedListener { _, destination, _ ->
            actualFragment = destination
        }

        //Search Bar Controller
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //val searchList = SearchList()

                if (query != null) {
                    if(actualFragment.id == R.id.na_fragment_map) {
                        Log.d("Nav Change", "Fragmento Mapa")
                        //....
                    }
                    if(actualFragment.id == R.id.na_fragment_ubicaciones) {
                        Log.d("Nav Change", "Fragmento Ubicaciones")
                        UbicacionesFragment().resetList()
                        UbicacionesFragment().updateRecyclerView(query)
                    }
                    if(actualFragment.id == R.id.na_fragment_eventos) {
                        Log.d("Nav Change", "Fragmento Eventos")
                        EventosFragment().resetList()
                        EventosFragment().updateRecyclerView(query)
                    }
                    if(actualFragment.id == R.id.na_fragment_horarios) {
                        Log.d("Nav Change", "Fragmento Horarios")
                        //HorariosFragment().resetList()
                        HorariosFragment().updateRecyclerView(query)
                    }
                    //UbicacionesFragment().updateRecyclerView(query)

                    Log.d("SearchList", "")
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //replaceFragment(SearchList())
                return false
            }
        })
        binding.searchBar.setOnQueryTextFocusChangeListener(object: View.OnFocusChangeListener{
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (!p1) {
                    if(actualFragment.id == R.id.na_fragment_map){}
                    if(actualFragment.id == R.id.na_fragment_ubicaciones) UbicacionesFragment().resetList()
                    if(actualFragment.id == R.id.na_fragment_eventos) EventosFragment().resetList()
                    if(actualFragment.id == R.id.na_fragment_horarios){}
                } else {
                    if(actualFragment.id == R.id.na_fragment_map) {
                        binding.navView.selectedItemId = R.id.na_fragment_ubicaciones
                    }
                }
            }

        })
    }
}