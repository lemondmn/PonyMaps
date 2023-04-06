package mx.edu.ubicatec.ponymaps.ui.mapa

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
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
import android.widget.TextView
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
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
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
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
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


class MapaFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback, LocationEngine {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapaViewModel: MapaViewModel

    private lateinit var thiscontext: Context

    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private var places: HashMap<String, Nodo> = HashMap()

    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private var isAnnoView = false
    private lateinit var currentAnnoView: String


    private var FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
    private var COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var mLocationPermissionGranted = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var routeDisplay = false
    private lateinit var currentPoint: Point
    private var currentRoute: DirectionsRoute? = null
    private lateinit var currentLine: LineString


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
                    Point.fromLngLat(-101.189156, 19.727334),// SW bounds
                    Point.fromLngLat(-101.181442, 19.716585),// NE bounds
                    true
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
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        thiscontext = container!!.context

        mapaViewModel = ViewModelProvider(requireActivity())[MapaViewModel::class.java]

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(thiscontext)


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
            val place = places[nombre]
            val dest: Point = Point.fromLngLat(place!!.lng, place.lat)
            moveCamera(dest)
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

        //On click Actions

        //Set Route
        binding.buttonRuta.setOnClickListener {
            val origen = spinnerOrigen.selectedItem.toString()
            val destino = spinnerDestino.selectedItem.toString()

            val ori = places[origen]
            val dest = places[destino]

            if (origen != destino) {
                //addRoute(origen, destino)
                binding.motionBase.transitionToStart()

                if(origen == "A" && destino == "B"){
                    currentPoint = Point.fromLngLat(  -101.23710525272159, 19.69449994262627)
                    getRoute( currentPoint, Point.fromLngLat( -101.23762846863752, 19.69412591073233))
                }
                //getRoute(Point.fromLngLat(ori!!.lng, ori.lat), Point.fromLngLat(dest!!.lng, dest.lat))

            }

            binding.motionBase.transitionToStart()
        }

        //Clear route
        binding.btnLimpiar.setOnClickListener {
            //clearRoute()
            binding.motionBase.transitionToStart()
        }
        //getCurrentLocation()

        return root
    }

    /** onViewCreated */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        mapboxMap = binding.mapView.getMapboxMap()

        mapboxMap.loadStyle(
            (
                    style("mapbox://styles/angels0107/clc5cffbm003r14ms47qqfzoi") {
                        +geoJsonSource(ROUTE_LINE_SOURCE_ID) {

                        }
                        +lineLayer(ROUTE_LAYER_ID, ROUTE_LINE_SOURCE_ID) {
                            lineColor(
                                ContextCompat.getColor(
                                    thiscontext,
                                    R.color.md_theme_light_primary
                                )
                            )
                            lineCap(LineCap.ROUND)
                            lineJoin(LineJoin.ROUND)
                            lineWidth(7.5)
                        }
                    }
                    )
        ) {
            setupBounds(ITM_BOUND)
        }

        onMapReady()

        locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
        locationPermissionHelper.checkPermissions {
            onLocationReady()
        }
    }

    /**
     *
     * APP-MAP FUNCTIONS
     *
     */
    private fun onMapReady() {

        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        viewAnnotationManager = binding.mapView.viewAnnotationManager

        mapboxMap.flyTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(-101.18544, 19.72176))
                .zoom(15.0)
                .build(),
            mapAnimationOptions {
                duration(2_000)
            }
        )

        setLocations()
        setAnnotationListeners()

    }


    private fun onLocationReady() {

        getLocationPermission()
        initLocationComponent()

    }

    private fun initLocationComponent() {

        val locationComponentPlugin = mapView.location
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

    }

    private fun setupBounds(bounds: CameraBoundsOptions) {
        mapboxMap.setBounds(bounds)
    }

    private fun setLocations() {
        /**
        ¡ NEEDS TO GET THE JSON FROM DB !
         */

        val jsonParser = JsonParser(resources)
        val jsonArr = jsonParser.getJSONArray(R.raw.places)

        for (i in 0 until jsonArr!!.length()) {

            // Create a JSONObject for fetching single User's Data
            val place = jsonArr.getJSONObject(i)
            // Fetch id store it in variable
            val name = place.getString("Nombre")
            val desc = place.getString("Descripcion")
            val lat = place.getString("Lat").toDouble()
            val lng = place.getString("Lng").toDouble()
            //val id = i.toLong()

            val obj = Nodo(name, desc, lat, lng, null, null)
            places[name] = obj

            // Now add all the variables to the data model class and the data model class to the array list.
            addAnnotationToMap(Point.fromLngLat(lng, lat), name)

        }
    }

    private fun addAnnotationToMap(point: Point, nodo: String) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            thiscontext,
            R.drawable.se_ic_map

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
            val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)

            val viewAnnotation = viewAnnotationManager.addViewAnnotation(
                resId = R.layout.annotation_view,
                options = viewAnnotationOptions {
                    geometry(point)
                    associatedFeatureId(pointAnnotation.featureIdentifier)
                    anchor(ViewAnnotationAnchor.BOTTOM)
                    offsetY((pointAnnotation.iconImageBitmap?.height!!).toInt() / 2)
                    visible(false)
                }

            )
            viewAnnotation.findViewById<TextView>(R.id.annotation).text = nodo
            //viewAnnotation.findViewById<TextView>(R.id.annotation).textAlignment = View.TEXT_ALIGNMENT_CENTER

            places[nodo]!!.id = pointAnnotation.featureIdentifier
            places[nodo]!!.id_view = viewAnnotation.id

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

    private fun setAnnotationListeners() {

        pointAnnotationManager.addClickListener(OnPointAnnotationClickListener {
            Toast.makeText(
                thiscontext,
                "Marker clicked, ID: ${it.id}, ID:${it.textField}",
                Toast.LENGTH_SHORT
            ).show()
            val place = places[it.textField]
            val dest: Point = Point.fromLngLat(place!!.lng, place.lat)

            moveCamera(dest)

            false
        })

        pointAnnotationManager.addLongClickListener(OnPointAnnotationLongClickListener {
            Toast.makeText(thiscontext, "Marker Long clicked", Toast.LENGTH_SHORT).show()
            val place = places[it.textField]

            toggleAnnotationVisibility(place!!)

            false
        })
    }

    private fun toggleAnnotationVisibility(place: Nodo) {

        if (!isAnnoView) {

            val viewAnnotation =
                viewAnnotationManager.getViewAnnotationByFeatureId(place.id.toString())

            viewAnnotationManager.updateViewAnnotation(
                viewAnnotation!!,
                options = viewAnnotationOptions {
                    visible(true)
                }
            )
            currentAnnoView = place.id.toString()
            isAnnoView = true

        } else {

            var viewAnnotation = viewAnnotationManager.getViewAnnotationByFeatureId(currentAnnoView)

            viewAnnotationManager.updateViewAnnotation(
                viewAnnotation!!,
                options = viewAnnotationOptions {
                    visible(false)
                }
            )

            viewAnnotation = viewAnnotationManager.getViewAnnotationByFeatureId(place.id.toString())

            viewAnnotationManager.updateViewAnnotation(
                viewAnnotation!!,
                options = viewAnnotationOptions {
                    visible(true)
                }
            )
            currentAnnoView = place.id.toString()

        }
    }

    private fun moveCamera(destination: Point) {

        val cameraPosition = CameraOptions.Builder()
            .zoom(18.0)
            .center(destination)
            .build()
        mapboxMap.flyTo(
            cameraPosition,
            mapAnimationOptions {
                duration(2_000)
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {


        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }
        ).addOnSuccessListener { location: Location? ->
            if (location == null)
                Toast.makeText(thiscontext, "Cannot get location.", Toast.LENGTH_SHORT).show()
            else {
                val lat = location.latitude
                val lon = location.longitude
                currentPoint = Point.fromLngLat(lon, lat)
                Toast.makeText(thiscontext, " Location: $lat $lon", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun locationUpdates() {

        val lifecycle = viewLifecycleOwner // in Fragment

        lifecycle.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // this block is automatically executed when moving into
                // the started state, and cancelled when stopping.
                //getCurrentLocation()
                currentLine = LineString.fromPolyline(
                    currentRoute!!.geometry()!!,
                    PRECISION_6
                )

                while (true) {

                    if (currentRoute != null) {

                        newLineCoordinates()
                        getCurrentLocation()


                        //Toast.makeText(thiscontext, " Location", Toast.LENGTH_SHORT).show()
                        drawNavigationPolylineRoute(currentLine)

                    }

                    delay(3 * 500)
                }


            }
        }


    }

    private fun newLineCoordinates(){

        currentLine.coordinates().removeAt(0)
        currentLine.coordinates().add(0, currentPoint)

        val nextPoint = currentLine.coordinates()[1]

        val dis = TurfMeasurement.distance(currentPoint, nextPoint, TurfConstants.UNIT_METERS)

        if ( dis <= 3.0 ){

            val points = currentLine.coordinates().toMutableList()
            if( points.isNotEmpty() ) points.removeAt(1)

            var newLine = LineString.fromLngLats(points)

            Toast.makeText(thiscontext, " Location " + newLine.coordinates(), Toast.LENGTH_SHORT).show()

            currentLine = newLine

        }

        /*var points = currentLine.coordinates().toMutableList()
        if( points.isNotEmpty() ) points.removeAt(0)

        var newLine = LineString.fromLngLats(points)

        Toast.makeText(thiscontext, " Location " + newLine.coordinates(), Toast.LENGTH_SHORT).show()

        currentLine = newLine


        val current = currentPoint*/



    }

    private fun getLocationPermission() {

        val permission = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(
                thiscontext,
                FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    thiscontext,
                    COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionGranted = true
                //initializeMap()


            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permission,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getRoute(origin: Point, destination: Point) {

        moveCamera(origin)

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
                currentRoute = response.body()!!.routes()[0]

                //drawNavigationPolylineRoute(currentRoute)
                displayRoute()
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

    private fun displayRoute() {

        routeDisplay = true

        locationUpdates()


    }

    private fun drawNavigationPolylineRoute(currentLine: LineString) {


        mapboxMap.getStyle { style ->
            // Retrieve and update the source designated for showing the directions route
            val lineLayerRouteGeoJsonSource: GeoJsonSource =
                style.getSourceAs(ROUTE_LINE_SOURCE_ID)!!

            val directionsRouteFeature = Feature.fromGeometry(
                currentLine
            )
            /*
            val a = LineString.fromPolyline(
                route.geometry()!!,
                PRECISION_6
            )
            a.coordinates()
            Toast.makeText(
                thiscontext,
                "Geometry: " + a.coordinates(), Toast.LENGTH_SHORT

            ).show()
            Toast.makeText(
                thiscontext,
                "Geometry: " + route.geometry()!!, Toast.LENGTH_SHORT
            ).show()
            */

            // Create a LineString with the directions route's geometry and
            // reset the GeoJSON source for the route LineLayer source
            if (lineLayerRouteGeoJsonSource != null) {
                // Create the LineString from the list of coordinates and then make a GeoJSON
                // FeatureCollection so we can add the line to our map as a layer.
                lineLayerRouteGeoJsonSource.feature(directionsRouteFeature)
            }

        }

    }

    /*private fun drawNavigationPolylineRoute(route: DirectionsRoute) {

        if (route != null && mapboxMap != null) {

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
                /*
                val a = LineString.fromPolyline(
                    route.geometry()!!,
                    PRECISION_6
                )
                a.coordinates()
                Toast.makeText(
                    thiscontext,
                    "Geometry: " + a.coordinates(), Toast.LENGTH_SHORT

                ).show()
                Toast.makeText(
                    thiscontext,
                    "Geometry: " + route.geometry()!!, Toast.LENGTH_SHORT
                ).show()
                */

                // Create a LineString with the directions route's geometry and
                // reset the GeoJSON source for the route LineLayer source
                if (lineLayerRouteGeoJsonSource != null) {
                    // Create the LineString from the list of coordinates and then make a GeoJSON
                    // FeatureCollection so we can add the line to our map as a layer.
                    lineLayerRouteGeoJsonSource.feature(directionsRouteFeature)
                }
            }
        } else {
            //Timber.d("Directions route is null")
            Toast.makeText(
                thiscontext,
                "Error, cant show route", Toast.LENGTH_SHORT
            ).show()
        }
    }*/

    /**
     *
     * APP STATES
     *
     */
    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
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