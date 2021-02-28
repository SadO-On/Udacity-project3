package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map : GoogleMap
    private lateinit var marker : Marker
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val REQUEST_LOCATION_PERMISSION = 1
    private var selectedLocation :  LatLng =  LatLng(0.0,0.0)
    private var poiName:String =""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object{
        private const val DEFAULT_ZOOM = 10
        private const val  TAG = "mapGoogle"

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync (this)
        binding.saveBtn.setOnClickListener {
            onLocationSelected()

        }
        return binding.root
    }


    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        Log.i(TAG , "enter OnMapReady")
        enableMyLocation()
        Log.i(TAG , "finish Calling enableMyLocation")
        map.uiSettings.isZoomControlsEnabled =true
        getDeviceLocation()
        setMapStyle(map)
        Toast.makeText(activity ,"Select place by performing a long press or click on POI." ,Toast.LENGTH_LONG).show()
        setMapLongClick(map)
        setPoiClick(map)
    }
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            poiName ="UNKNOWN PLACE"
             map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
            selectedLocation = latLng
            Log.i(TAG , selectedLocation.toString())

        }
    }

        private fun getDeviceLocation() {
        try{
            if(isPermissionGranted()){
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(activity!!){task ->
                    if(task.isSuccessful){
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(task.result!!.latitude,task.result!!.longitude)
                            ,DEFAULT_ZOOM.toFloat()))
                    }
                }
            }

        }catch (e:SecurityException){
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun isPermissionGranted() : Boolean {
        Log.i(TAG , "Enter isPermissionGranted")

        val foregroundPermission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(
                            activity!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return  foregroundPermission && backgroundPermissionApproved
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        Log.i(TAG , "Enter onRequestPermissionsResult")
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            Toast.makeText(activity , "You should allow location",Toast.LENGTH_LONG).show()
            Snackbar.make(
                activity!!.findViewById(R.id.map_fragment),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()


        } else {
            enableMyLocation()
        }
    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            Log.i(TAG , "Permission Granted")

            map.isMyLocationEnabled = true
        }
        else {
            Log.i(TAG , "Permission Denied")
            var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode = when {
                runningQOrLater -> {
                    permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                }
                else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            }
            Log.d(TAG, "Request foreground only location permission")

            requestPermissions(
                permissionsArray,
                resultCode
            )
        }}



        private fun onLocationSelected() {
            //To ensure that the user choose a place
            if(selectedLocation != LatLng(0.0,0.0)){
                _viewModel.latitude.value = selectedLocation.latitude
                _viewModel.longitude.value =selectedLocation.longitude
                _viewModel.reminderSelectedLocationStr.value = poiName
                findNavController().popBackStack()

            }else{
                Toast.makeText(activity , "Select place by performing a long press or click on POI.",Toast.LENGTH_LONG).show()
            }
    }

    private fun setPoiClick(map:GoogleMap){
       map.setOnPoiClickListener { pointOfInterest ->
           map.clear()
         val poiMarker=  map.addMarker(MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name))
           selectedLocation = pointOfInterest.latLng
           poiName = pointOfInterest.name
           Log.i(TAG,poiName)
           Log.i(TAG , selectedLocation.toString())
           poiMarker.showInfoWindow()

       }
        //(37.41904306583025,-122.0816123485565)
        //(37.41717481811686,-122.10841298103334)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID

            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE

            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity!!,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

    }

}
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1