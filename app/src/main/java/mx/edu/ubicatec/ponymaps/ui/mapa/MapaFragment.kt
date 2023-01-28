package mx.edu.ubicatec.ponymaps.ui.mapa

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentMapBinding
import mx.edu.ubicatec.ponymaps.models.ubicacion.Nodo
import mx.edu.ubicatec.ponymaps.utils.JsonParser
import mx.edu.ubicatec.ponymaps.utils.LocationPermissionHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

var mapView: MapView? = null

@Suppress("SAFE_CALL_WILL_CHANGE_NULLABILITY")
class MapaFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback, LocationEngine {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapaViewModel: MapaViewModel

    // Flag indicating whether a requested permission has been denied after returning in * [.onRequestPermissionsResult].
    private var permissionDenied = false

    private lateinit var thiscontext: Context

    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        //mapView!!.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView!!.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView!!.gestures.focalPoint = mapView!!.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private val routeDisplay = false
    private var currentPoint: Point? = null

    private var places : HashMap<String, Nodo> = HashMap()
    val items: List<String> = ArrayList()

    private lateinit var  annotationApi : AnnotationPlugin
    private lateinit var pointAnnotationManager : PointAnnotationManager

    /**
     *
     * MAPBOX VALUES
     *
     */
    companion object {

        private const val ROUTE_LAYER_ID = "route-layer-id"
        private const val ROUTE_LINE_SOURCE_ID = "route-source-id"

        private val ITM_BOUND: CameraBoundsOptions = CameraBoundsOptions.Builder()
            .bounds(
                CoordinateBounds(
                    Point.fromLngLat( -101.189156, 19.727334),// SW bounds
                    Point.fromLngLat( -101.181442, 19.716585),// NE bounds
                    false
                )
            )
            .minZoom(2.0)
            .build()
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

        mapaViewModel = ViewModelProvider(requireActivity())[MapaViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(thiscontext)

        /**
         *
         * Datos que envian los otros fragmentos aqui
         *
         */

        mapaViewModel.idEvento.observe(viewLifecycleOwner) { id ->
            Toast.makeText(requireContext(), "ID: $id", Toast.LENGTH_LONG).show()
        }

        mapaViewModel.nombreSalon.observe(viewLifecycleOwner) { nombre ->
            Toast.makeText(requireContext(), "NombreSalon: $nombre", Toast.LENGTH_LONG).show()
        }

        mapaViewModel.nombreUbicacion.observe(viewLifecycleOwner) { nombre ->
            Toast.makeText(requireContext(), "NombreUbicacion: $nombre", Toast.LENGTH_LONG).show()
        }


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
        /*
        var languages = arrayOf("English", "French", "Spanish", "Hindi", "Russian", "Telugu", "Chinese", "German", "Portuguese", "Arabic", "Dutch", "Urdu", "Italian", "Tamil", "Persian", "Turkish", "Other")
        ArrayAdapter(requireContext(), R.layout.custom_spinner_item, languages).also { adapter ->
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            spinnerOrigen.adapter = adapter
            spinnerDestino.adapter = adapter
        }*/

        //On click Actions

        //Set Route
        binding.buttonRuta.setOnClickListener {


            val origen = spinnerOrigen.selectedItem.toString()
            val destino = spinnerDestino.selectedItem.toString()


            if (origen != destino){
                //addRoute(origen, destino)
                binding.motionBase.transitionToStart()
            }

            val ori = places[origen]
            val dest = places[destino]

            getRoute(Point.fromLngLat(ori!!.lng,ori.lat),Point.fromLngLat(dest!!.lng, dest.lat))
            /*
            if (origen == "AG" && destino == "S"){
                val ori: Point = Point.fromLngLat(-101.184121, 19.723182)
                val dest: Point = Point.fromLngLat(-101.186971,19.721577)
                getRoute(ori, dest)
            }
            if (origen == "A" && destino == "E"){
                val ori: Point = Point.fromLngLat(-101.185809,19.722984)
                val dest: Point = Point.fromLngLat(-101.185283,19.722697)
                getRoute(ori, dest)
            }*/

        }

        //Clear route
        binding.btnLimpiar.setOnClickListener {
            //clearRoute()
            binding.motionBase.transitionToStart()
        }
        getCurrentLocation()

        return root
    }

    /** onViewCreated */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        mapboxMap = binding.mapView.getMapboxMap()

        mapView!!.getMapboxMap().loadStyle(
            (
                    style("mapbox://styles/angels0107/clc5cffbm003r14ms47qqfzoi") {
                        +geoJsonSource(ROUTE_LINE_SOURCE_ID) {

                        }
                        +lineLayer(ROUTE_LAYER_ID, ROUTE_LINE_SOURCE_ID) {
                            lineColor(ContextCompat.getColor(thiscontext, R.color.md_theme_light_primary))
                            lineCap(LineCap.ROUND)
                            lineJoin(LineJoin.ROUND)
                            lineWidth(5.0)
                        }
                    }
                    )
        )

        startUpdates()
        /*/var a = places[""]
        a?.isDraggable = true*/

        locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }



    }

    /**
     *
     * APP-MAP FUNCTIONS
     *
     */

    private fun onMapReady() {

        annotationApi = mapView?.annotations!!
        pointAnnotationManager = annotationApi.createPointAnnotationManager()

        initLocationComponent()
        setupGesturesListener()
        setupBounds(ITM_BOUND)

        mapView!!.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat( -101.18544, 19.72176))
                .zoom(15.0)
                .build()
        )
        setLocations()

        //Click Listeners

        /*
        pointAnnotationManager.apply {
            this!!.addClickListener(
                OnPointAnnotationClickListener { it1 ->
                    Toast.makeText(thiscontext, "id: ${it1.id}", Toast.LENGTH_LONG).show()
                    false
                }
            )
        }*/

        pointAnnotationManager.addClickListener(OnPointAnnotationClickListener {
            Toast.makeText(thiscontext, "Marker clicked, ID: ${it.id}, ID:${it.textField}", Toast.LENGTH_SHORT).show()

            false
        })

        pointAnnotationManager.addLongClickListener( OnPointAnnotationLongClickListener {
            Toast.makeText(thiscontext, "Marker Long clicked", Toast.LENGTH_SHORT).show()
            false
        })

    }
    private fun setupGesturesListener() {
        mapView!!.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView!!.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    thiscontext,
                    com.mapbox.maps.R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    thiscontext,
                    com.mapbox.maps.R.drawable.mapbox_user_icon_shadow,
                ),

                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.025)
                    }
                    stop {
                        literal(20.0)
                        literal(0.025)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }
    private fun onCameraTrackingDismissed() {
        //Toast.makeText(thiscontext, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView!!.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView!!.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView!!.gestures.removeOnMoveListener(onMoveListener)
    }

    private fun setupBounds(bounds: CameraBoundsOptions) {
        mapboxMap.setBounds(bounds)
    }

    private fun getRoute(origin: Point ,destination: Point) {

        val token: String = getString(R.string.mapbox_access_token)
        val route = listOf(
            origin, // origin
            destination // destination
        )
        /*
            listOf(
                        Point.fromLngLat(-101.184121, 19.723182), // origin
                        Point.fromLngLat(-101.186971,19.721577) // destination
                    )
         */

        val client = MapboxDirections.builder()
            .accessToken(token)
            .routeOptions(
                RouteOptions.builder()
                    .coordinatesList(
                        route
                    )
                    .profile(DirectionsCriteria.PROFILE_WALKING)
                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                    .build()
            )
            .build()

        client.enqueueCall(object : Callback<DirectionsResponse?> {
            override fun onResponse(
                call: Call<DirectionsResponse?>,
                response: Response<DirectionsResponse?>
            ) {
                if (response.body() == null) {
                    // Log.e("No routes found, make sure you set the right user and access token.")
                    //Timber.d("No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    // Log.e("No routes found)
                    return
                }
                val currentRoute = response.body()!!.routes()[0]
                println(currentRoute)
                println("Fuuuuuuuuuck")

                drawNavigationPolylineRoute(currentRoute)
            }

            override fun onFailure(call: Call<DirectionsResponse?>, throwable: Throwable) {
                //Timber.d("Error: %s", throwable.message)
                if (throwable.message != "Coordinate is invalid: 0,0") {
                    Toast.makeText(
                        thiscontext,
                        "Error: " + throwable.message, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    }
    private fun drawNavigationPolylineRoute(route: DirectionsRoute ) {

        if (route != null) {
            if (mapboxMap != null) {

                mapboxMap.getStyle { style ->
                    // Retrieve and update the source designated for showing the directions route
                    val lineLayerRouteGeoJsonSource: GeoJsonSource =
                        style.getSourceAs(ROUTE_LINE_SOURCE_ID)!!

                    val directionsRouteFeature = Feature.fromGeometry(
                        LineString.fromPolyline(
                            route.geometry()!!,
                            PRECISION_6
                        )
                    )

                    // Create a LineString with the directions route's geometry and
                    // reset the GeoJSON source for the route LineLayer source
                    if (lineLayerRouteGeoJsonSource != null) {
                        // Create the LineString from the list of coordinates and then make a GeoJSON
                        // FeatureCollection so we can add the line to our map as a layer.
                        lineLayerRouteGeoJsonSource.feature(directionsRouteFeature)
                    }
                }


            }
        } else {
            //Timber.d("Directions route is null")
            Toast.makeText(
                thiscontext,
                "Error, cant show route", Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun addAnnotationToMap(point: Point, nodo: String) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            thiscontext,
            R.drawable.red_marker

        )?.let {
            /*
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
            */

            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(point)
                // Specify the bitmap you assigned to the point annotation
                // The bitmap will be added to map style automatically.
                .withIconImage(it)
                .withIconSize(0.5)
                .withTextField(nodo)
                .withTextSize(0.0)

            // Add the resulting pointAnnotation to the map.
            var pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
            places[nodo]!!.id = pointAnnotation.id

            /*var last = pointAnnotationManager?.annotations!!.last()
            places.put("",last)*/
        }
    }
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                val lat = location?.latitude
                val lon = location?.longitude
                currentPoint = Point.fromLngLat(lon!!, lat!!)
                if (lat != null && lon != null){
                    //Toast.makeText(thiscontext, "Current location: $lat , $lon", Toast.LENGTH_SHORT).show()
                    Toast.makeText(thiscontext, "Current location: $currentPoint , $lon", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startUpdates() {

        val lifecycle = viewLifecycleOwner // in Fragment

        lifecycle.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // this block is automatically executed when moving into
                // the started state, and cancelled when stopping.
                while (routeDisplay) {
                    getCurrentLocation()
                    val dest: Point = Point.fromLngLat(-101.23712022352315,19.695377903734485)
                    if(currentPoint != null)
                    //getRoute(currentPoint!!,dest)
                        delay(3 * 1000)
                }
            }
        }
    }

    private fun setLocations(){
        /**
        NEEDS TO GET THE JSON FROM DB
        */

        val jsonParser = JsonParser(resources)
        val jsonArr = jsonParser.getJSONArray(R.raw.places)

        println("JSONNNNNNN")
        println(jsonArr)

        for (i in 0 until jsonArr!!.length()) {

            // Create a JSONObject for fetching single User's Data
            val place = jsonArr.getJSONObject(i)
            // Fetch id store it in variable
            val name = place.getString("Nombre")
            val desc = place.getString("Descripcion")
            val lat = place.getString("Lat").toDouble()
            val lng = place.getString("Lng").toDouble()
            val id = i.toLong()

            val obj = Nodo(name,desc,lat,lng,null)
            places[name] = obj

            // Now add all the variables to the data model class and the data model class to the array list.
            addAnnotationToMap(Point.fromLngLat(lng, lat), name)

        }
        /*
        println("IDSSSSSSSSSSSSSSSSSSS")
        for (place in places){
            val a = place.value
            println(a.id)
        }*/

    }
    /**
     *
     * APP STATES
     *
     */
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView!!.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView!!.gestures.removeOnMoveListener(onMoveListener)
        mapView?.onDestroy()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
        TODO("Not yet implemented")
    }

    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper?
    ) {
        TODO("Not yet implemented")
    }

    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        pendingIntent: PendingIntent?
    ) {
        TODO("Not yet implemented")
    }

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {
        TODO("Not yet implemented")
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
        TODO("Not yet implemented")
    }


}