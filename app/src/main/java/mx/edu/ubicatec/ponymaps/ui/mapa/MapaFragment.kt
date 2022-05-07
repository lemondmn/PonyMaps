package mx.edu.ubicatec.ponymaps.ui.mapa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

import mx.edu.ubicatec.ponymaps.MainActivity
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentMapBinding

class MapaFragment : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        googleMap.setMinZoomPreference(MapaFragment.DEFAULT_MIN_ZOOM);
        googleMap.setMaxZoomPreference(MapaFragment.DEFAULT_MAX_ZOOM);
        googleMap.setLatLngBoundsForCameraTarget(MapaFragment.ITM);
    }

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val mapaViewModel = ViewModelProvider(this).get(MapaViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    companion object {
        private val TAG = MapaFragment::class.java.name
        private const val ZOOM_DELTA = 2.0f
        private const val DEFAULT_MIN_ZOOM = 17.0f
        private const val DEFAULT_MAX_ZOOM = 22.0f
        private val ITM = LatLngBounds(
            LatLng(19.719593, -101.187720), // SW bounds
            LatLng(19.724298, -101.182758) // NE bounds
        )
        private val ITM_CAMERA = CameraPosition.Builder()
            .target(LatLng(19.722037, -101.184835)).zoom(15.0f).bearing(0f).tilt(0f).build()

    }
}