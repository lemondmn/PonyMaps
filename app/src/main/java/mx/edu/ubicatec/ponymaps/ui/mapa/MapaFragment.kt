package mx.edu.ubicatec.ponymaps.ui.mapa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.geojson.GeoJsonLayer
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentMapBinding
import mx.edu.ubicatec.ponymaps.models.PermissionUtils
import mx.edu.ubicatec.ponymaps.models.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import mx.edu.ubicatec.ponymaps.models.PermissionUtils.isPermissionGranted


class MapaFragment : Fragment(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Flag indicating whether a requested permission has been denied after returning in * [.onRequestPermissionsResult].
    private var permissionDenied = false

    private lateinit var map: GoogleMap
    private lateinit var thiscontext: Context

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

        // Request code for location permission request.
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

    }

    /**
     *
     * GOOGLE MAPS
     *
     */

    private val callback = OnMapReadyCallback { googleMap ->

        /* Manipulates the map once available.
        * This callback is triggered when the map is ready to be used.
        * This is where we can add markers or lines, add listeners or move the camera.
        * If Google Play services is not installed on the device, the user will be prompted to
        * install it inside the SupportMapFragment. This method will only be triggered once the
        user has installed Google Play services and returned to the app.
        */

        map = googleMap

        googleMap.setMinZoomPreference(DEFAULT_MIN_ZOOM)
        googleMap.setMaxZoomPreference(DEFAULT_MAX_ZOOM)
        googleMap.setLatLngBoundsForCameraTarget(ITM)
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(ITM_CAMERA))

        // Custom map style

        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(thiscontext, R.raw.mapstyle))
        } catch (e: NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        /* val layer = KmlLayer(googleMap, R.raw.ponymaps, context);
         layer.addLayerToMap();*/

        val layer = GeoJsonLayer(googleMap, R.raw.jsonmaps, context)
        layer.addLayerToMap()

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

        //Location
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        //enableMyLocation()

    }

    /**
     *
     * Fragment
     *
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val mapaViewModel = ViewModelProvider(this).get(MapaViewModel::class.java)
        thiscontext  = container!!.getContext()

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

    }

    /**
     *
     * LOCATION
     *
     */

     // Enables the My Location layer if the fine location permission has been granted.

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // [START maps_check_location_permission]
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                thiscontext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                thiscontext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity?.parent!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                activity?.parent!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(childFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            activity?.parent!!,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
        // [END maps_check_location_permission]
    }


    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(thiscontext, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(thiscontext, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResume() {
        super.onResume()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(childFragmentManager, "dialog")
    }



}