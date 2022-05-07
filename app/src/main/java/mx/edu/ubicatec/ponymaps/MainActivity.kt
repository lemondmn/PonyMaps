package mx.edu.ubicatec.ponymaps

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import mx.edu.ubicatec.ponymaps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        navView.setupWithNavController(navController)
    }

    /* override fun onMapReady(googleMap: GoogleMap) {

        googleMap.setMinZoomPreference(DEFAULT_MIN_ZOOM);
        googleMap.setMaxZoomPreference(DEFAULT_MAX_ZOOM);
        googleMap.setLatLngBoundsForCameraTarget(ITM);

    }

    companion object {
        private val TAG = MainActivity::class.java.name
        private const val ZOOM_DELTA = 2.0f
        private const val DEFAULT_MIN_ZOOM = 17.0f
        private const val DEFAULT_MAX_ZOOM = 22.0f
        private val ITM = LatLngBounds(
            LatLng(19.719593, -101.187720), // SW bounds
            LatLng(19.724298, -101.182758) // NE bounds
        )
        private val ITM_CAMERA = CameraPosition.Builder()
            .target(LatLng(19.722037, -101.184835)).zoom(15.0f).bearing(0f).tilt(0f).build()

    } */

}