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
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
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
import com.google.maps.android.data.LineString
import com.google.maps.android.data.geojson.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*

import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentMapBinding
import mx.edu.ubicatec.ponymaps.models.PermissionUtils
import mx.edu.ubicatec.ponymaps.models.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import mx.edu.ubicatec.ponymaps.models.PermissionUtils.isPermissionGranted
import mx.edu.ubicatec.ponymaps.models.Edges
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class MapaFragment : Fragment(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // Flag indicating whether a requested permission has been denied after returning in * [.onRequestPermissionsResult].
    private var permissionDenied = false

    private lateinit var map: GoogleMap
    private lateinit var layermap: GeoJsonLayer
    private var ruta: GeoJsonFeature? = null
    private lateinit var thiscontext: Context

    private var edges = mutableListOf<Edges>()
    private var nodes = mutableListOf<GeoJsonFeature>()

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

        googleMap.uiSettings.isTiltGesturesEnabled = false

        /** Custom map style */
        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(thiscontext, R.raw.mapstyle))
        } catch (e: NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        /** Remove Default Buttons */



        /** SETS BOUNDS */
        googleMap.setMinZoomPreference(DEFAULT_MIN_ZOOM)
        googleMap.setMaxZoomPreference(DEFAULT_MAX_ZOOM)
        googleMap.setLatLngBoundsForCameraTarget(ITM)
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(ITM_CAMERA))

        val layer = GeoJsonLayer(googleMap, R.raw.jsonmaps, context)
        layer.addLayerToMap()
        layermap = layer

        setMarkerNodes(layer)
        setListeners(layer)

        val pointPolygonStyle = layer.defaultPolygonStyle
        pointPolygonStyle.fillColor = Color.argb(128,0,160,227)
        pointPolygonStyle.strokeColor = Color.argb(128,0,160,227)

        /** SETS GPS */
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()

    }

    /**
     *
     * Fragment
     *
     */

    /** onCreateView */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        thiscontext  = container!!.context

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Spinner
        val spinnerOrigen: Spinner = binding.spOrigen
        val spinnerDestino: Spinner = binding.spDestino

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ubicaciones,
            R.layout.custom_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            spinnerOrigen.adapter = adapter
            spinnerDestino.adapter = adapter
        }

        //On click Actions

        //Set Route
        binding.buttonRuta.setOnClickListener {

            val origen = spinnerOrigen.selectedItem.toString()
            val destino = spinnerDestino.selectedItem.toString()

            addRoute(origen, destino)

            binding.motionBase.transitionToStart()
        }

        //Clear route
        binding.btnLimpiar.setOnClickListener {
            clearRoute()
            binding.motionBase.transitionToStart()
        }

        return root
    }

    /** onViewCreated */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment?.getMapAsync(callback)

        try {
            // As we have JSON object, so we are getting the object
            //Here we are calling a Method which is returning the JSON object

            val obj = JSONObject(getJSONFromAssets()!!)

            // fetch JSONArray named edges by using getJSONArray
            val usersArray = obj.getJSONArray("edges")

            for (i in 0 until usersArray.length()) {
                // Create a JSONObject for fetching single User's Data
                val edge = usersArray.getJSONObject(i)
                // Fetch id store it in variable
                val origen = edge.getString("origen")
                val destino = edge.getString("destino")
                val dist = edge.getInt("dist")

                // Now add all the variables to the data model class and the data model class to the array list.
                val edg = Edges(origen, destino, dist)

                // add the details in the list
                edges.add(edg)
            }

        } catch (e: JSONException) {
            //exception
            e.printStackTrace()
        }

    }

    /**
     *
     * LAYER & NODES
     *
     */

    private fun setMarkerNodes(layer: GeoJsonLayer) {

        // Iterate over all the features stored in the layer
        for (feature in layer.features) {

            // Check if the node property exists
            if (feature.getProperty("node") != null ) {

                nodes.add(feature)

                if(feature.getProperty("node") == "place" ){

                    val name = feature.getProperty("name")

                    // Create a new point style
                    val pointStyle = GeoJsonPointStyle()

                    // Set options for the point style
                    pointStyle.title = name
                    pointStyle.alpha = 0.3f

                    // Assign the point style to the feature
                    feature.pointStyle = pointStyle

                }
                else if (feature.getProperty("node") == "path"){

                    // Create a new point style
                    val pointStyle = GeoJsonPointStyle()

                    // Set options for the point style
                    pointStyle.isVisible = false

                    // Assign the point style to the feature
                    feature.pointStyle = pointStyle

                }
            }
        }
    }
    private fun setListeners(layer: GeoJsonLayer) {

        layer.setOnFeatureClickListener { feature ->

            Log.i("GeoJsonClick", "Feature clicked: ${feature.getProperty("name")}")

        }

    }

    private fun getJSONFromAssets(): String? {

        var json: String? = null
        val charset: Charset = Charsets.UTF_8
        try {
            val myUsersJSONFile = resources.openRawResource(R.raw.distances)
            val size = myUsersJSONFile.available()
            val buffer = ByteArray(size)
            myUsersJSONFile.read(buffer)
            myUsersJSONFile.close()
            json = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    /**
     *
     * Routing
     *
     */

    private fun dijkstra(source: String, destiny: String): MutableList<String> {

        // Initialize single source

        var d : HashMap<String, Int> = HashMap<String, Int> ()
        var pi : HashMap<String, String> = HashMap<String, String> ()

        for (feature in nodes) {

            d.put(feature.getProperty("name") , 9999)
            pi.put(feature.getProperty("name"), "-")

        }

        d.put(source , 0)
        pi.put(source, "-")

        val S: MutableList<GeoJsonFeature> = ArrayList()
        val Q: MutableList<GeoJsonFeature> = nodes.toMutableList()

        while (Q.isNotEmpty()) {

            val u: GeoJsonFeature = extractMin( Q, d)
            S.add(u)

            val name = u.getProperty("name")

            for (edge in edges) {
                if(edge.origen  == name){

                    if( d[edge.destino]!! > ( d[name]!! + edge.dist)) {

                        d[edge.destino] = ( d[name]!! + edge.dist )
                        pi[edge.destino] = name

                    }
                }
            }
        }

        var dest = destiny
        var tmp = dest

        val route: MutableList<String> = ArrayList()

        while (tmp != source) {

            route.add(tmp)
            tmp = pi[tmp]!!

        }

        return route

    }
    private fun extractMin( Q: MutableList<GeoJsonFeature>, d : HashMap<String, Int>): GeoJsonFeature {

        var minNode = Q[0]
        var minDistance: Int? = d[ Q[0].getProperty("name") ]

        for (feature in Q) {

            if ( d[ feature.getProperty("name") ]!! < minDistance!!) {
                minNode = feature
                minDistance = d[ feature.getProperty("name") ]
            }

        }

        Q.remove(minNode)
        return minNode

    }

    private fun setRoute(source: String, destiny: String): GeoJsonFeature {

        val route = dijkstra(source, destiny)

        val lineStringArray: MutableList<LatLng> = ArrayList()

        for (i in route){

            for (feature in nodes){

                if(i == feature.getProperty("name") ){

                    var co = feature.geometry.geometryObject
                    val coo: LatLng = co as LatLng

                    lineStringArray.add(coo)

                }
            }
        }
        for (feature in nodes){

            if(source == feature.getProperty("name") ){

                var co = feature.geometry.geometryObject
                val coo: LatLng = co as LatLng

                lineStringArray.add(coo)

            }
        }

        val lineString = GeoJsonLineString(lineStringArray)
        val lineStringFeature = GeoJsonFeature(lineString, null, null, null)

        // Set the color of the linestring to red
        val lineStringStyle = GeoJsonLineStringStyle()
        lineStringStyle.color = R.color.primaryShade1

        // Set the style of the feature
        lineStringFeature.lineStringStyle = lineStringStyle

        return lineStringFeature
    }

    private fun addRoute(source: String, destiny: String){

        if (ruta != null) layermap.removeFeature(ruta)

        val a = setRoute(source, destiny)
        ruta = a
        layermap.addFeature(a)

    }

    private fun clearRoute(){

        if (ruta != null) layermap.removeFeature(ruta)

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
                //activity?.parent!!,
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
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
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
        // [END maps_check_location_permission]
    }


    override fun onMyLocationButtonClick(): Boolean {
        //Toast.makeText(thiscontext, "MyLocation button clicked", Toast.LENGTH_SHORT) .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(thiscontext, "Current location:\n$location", Toast.LENGTH_LONG).show()
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