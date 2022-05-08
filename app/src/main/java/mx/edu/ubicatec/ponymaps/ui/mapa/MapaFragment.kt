package mx.edu.ubicatec.ponymaps.ui.mapa


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.kml.KmlLayer
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentMapBinding
import org.json.JSONObject

class MapaFragment : Fragment() {

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
    /**
     *
     * GOOGLE MAPS
     *
     */

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        googleMap.setMinZoomPreference(DEFAULT_MIN_ZOOM);
        googleMap.setMaxZoomPreference(DEFAULT_MAX_ZOOM);
        googleMap.setLatLngBoundsForCameraTarget(ITM);
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(ITM_CAMERA));

       /* val layer = KmlLayer(googleMap, R.raw.ponymaps, context);
        layer.addLayerToMap();*/

        val layer = GeoJsonLayer(googleMap, R.raw.jsonmaps, context);
        layer.addLayerToMap();

        val pointStyle = layer.defaultPointStyle
        pointStyle.alpha = 0.5f
        pointStyle.isDraggable = true
        pointStyle.title = "Hello, World!"
        pointStyle.snippet = "I am a draggable marker"

        val pointPolygonStyle = layer.defaultPolygonStyle
        pointPolygonStyle.fillColor = Color.argb(128,0,160,227)
        pointPolygonStyle.strokeColor = Color.argb(128,0,160,227)



        val iTM = LatLng(19.722037, -101.184835)
        googleMap.addMarker(
            MarkerOptions()
                .alpha(0.5F)
                .position(iTM)
                .title("Marker in Sydney")
        )


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
    /*: View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }/*: View {
        val mapaViewModel = ViewModelProvider(this).get(MapaViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }*/

}