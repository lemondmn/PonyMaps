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
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.android.core.location.*
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
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationLongClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentMapBinding
import mx.edu.ubicatec.ponymaps.utils.LocationPermissionHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

var mapView: MapView? = null

class MapaFragment() : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback, LocationEngine {

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

    /**
     *
     * MAPBOX VALUES
     *
     */
    companion object {

        private const val ROUTE_LAYER_ID = "route-layer-id"
        private const val ROUTE_LINE_SOURCE_ID = "route-source-id"

        private const val ZOOM_DELTA = 2.0f
        private const val DEFAULT_MIN_ZOOM = 17.0f
        private const val DEFAULT_MAX_ZOOM = 22.0f
        /*
        private val ITM = LatLngBounds(
            LatLng(19.719593, -101.187720), // SW bounds
            LatLng(19.724298, -101.182758) // NE bounds
        )
        private val ITM_CAMERA = CameraPosition.Builder()
            .target(LatLng(19.722037, -101.184835)).zoom(15.0f).bearing(0f).tilt(0f).build()*/

        private val ITM_BOUND: CameraBoundsOptions = CameraBoundsOptions.Builder()
            .bounds(
                CoordinateBounds(
                    Point.fromLngLat(-101.187720, 19.719593),// SW bounds
                    Point.fromLngLat(-101.182758, 19.724298),// NE bounds
                    true
                )
            )
            .minZoom(8.0)
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

        //On click Actions

        //Set Route
        binding.buttonRuta.setOnClickListener {


            val origen = spinnerOrigen.selectedItem.toString()
            val destino = spinnerDestino.selectedItem.toString()


            if (!(origen.equals(destino))){
                //addRoute(origen, destino)
                binding.motionBase.transitionToStart()
            }

            if (origen == "AG" && destino == "S"){
                val ori: Point = Point.fromLngLat(-101.184121, 19.723182)
                val dest: Point = Point.fromLngLat(-101.186971,19.721577)
                getRoute(ori, dest)
            }
            if (origen == "A" && destino == "E"){
                val ori: Point = Point.fromLngLat(-101.185809,19.722984)
                val dest: Point = Point.fromLngLat(-101.185283,19.722697)
                getRoute(ori, dest)
            }

            
        }

        //Clear route
        binding.btnLimpiar.setOnClickListener {
            //clearRoute()
            binding.motionBase.transitionToStart()
        }

        return root
    }

    /** onViewCreated */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        mapboxMap = binding.mapView.getMapboxMap()

        setupBounds(ITM_BOUND)

        mapView!!.getMapboxMap().loadStyle(
            (
                style("mapbox://styles/angels0107/clc5cffbm003r14ms47qqfzoi") {
                    +geoJsonSource(ROUTE_LINE_SOURCE_ID) {

                    }
                    +lineLayer(ROUTE_LAYER_ID, ROUTE_LINE_SOURCE_ID) {
                        lineColor(ContextCompat.getColor(thiscontext, R.color.purple_500))
                        lineCap(LineCap.ROUND)
                        lineJoin(LineJoin.ROUND)
                        lineWidth(5.0)
                    }
                }
            )
        )


        addAnnotationToMap()

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
        mapView!!.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        initLocationComponent()
        setupGesturesListener()
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
        Toast.makeText(thiscontext, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
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
    private fun addAnnotationToMap() {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            thiscontext,
            R.drawable.red_marker
        )?.let {
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)
            //Click Listeners
            pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener {
                Toast.makeText(thiscontext, "Marker clicked", Toast.LENGTH_SHORT).show()
                currentLocation()
                true
            })
            pointAnnotationManager?.addLongClickListener( OnPointAnnotationLongClickListener {
                Toast.makeText(thiscontext, "Marker Long clicked", Toast.LENGTH_SHORT).show()
                true
            })
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(Point.fromLngLat(-101.184121, 19.723182))
                // Specify the bitmap you assigned to the point annotation
                // The bitmap will be added to map style automatically.
                .withIconImage(it)

            // Add the resulting pointAnnotation to the map.
            pointAnnotationManager?.create(pointAnnotationOptions)
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

    private fun currentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                var lat = location?.latitude
                var lon = location?.longitude
                if (lat != null && lon != null){
                    Toast.makeText(thiscontext, "Current location: $lat , $lon", Toast.LENGTH_SHORT).show()
                }
            }
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