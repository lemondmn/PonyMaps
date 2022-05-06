package mx.edu.ubicatec.ponymaps.ui.mapa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider


import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

import mx.edu.ubicatec.ponymaps.MainActivity
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentMapBinding

/*class MapaFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val MAP_VIEW_BUNDLE_KEY = "mapview_bundle_key"
    }

    private var _binding: FragmentMapsBinding? = null
    private val binding: FragmentMapsBinding get() = _binding!!

    private var hasMapConfigured: Boolean = false
    // Used to persist map state during fragment transaction.
    private lateinit var persistedMapBundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        persistedMapBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY) ?: Bundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(layoutInflater, container, false)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        initMapViewState(binding.mapView, mapViewBundle)
        binding.mapView.getMapAsync(this)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(MAP_VIEW_BUNDLE_KEY, persistedMapBundle)
    }

    // region Typical lifecycle method forwarding
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        _binding?.mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onSaveInstanceState(persistedMapBundle)
        binding.mapView.onDestroy()
        _binding = null
    }
    // endregion

    private fun initMapViewState(mapView: MapView, savedMapViewBundle: Bundle?) {
        // The state persisted across Fragment transaction.
        if (!persistedMapBundle.isEmpty) {
            mapView.onCreate(persistedMapBundle)
            hasMapConfigured = true
            return
        }
        // The state persisted across Fragment recreation.
        mapView.onCreate(savedMapViewBundle)
        hasMapConfigured = savedMapViewBundle != null
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }
}*/

class MapaFragment : Fragment() {

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

        val textView: TextView = binding.textViewMap
        mapaViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}

/*
, OnMapReadyCallback

val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
mapFragment.getMapAsync(this)

 override fun onMapReady(googleMap: GoogleMap) {

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

    }

    */