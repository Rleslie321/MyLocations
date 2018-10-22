package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class MapActivity contains map fragment and handles longclick events and infowindow clicks*/
/* Author: Robert Leslie */
/* Notes: Uses deprecated FusedLocationApi because I couldn't figure out the new version*/
/* Reference: https://www.quickotlin.com/app/google-maps-api-current-location-kotlin-android/ */
/* http://freemusicarchive.org/music/Knit_Your_Own_Scarf/Agur/Selva_-_Agur_-_04_Ajedrez */
/* Last Modified: 6/8/2018 */
/* ********************************** */

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnInfoWindowClickListener{
    //global args
    private var googlemap: GoogleMap? = null
    private var googleApiClient: GoogleApiClient? = null
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private var mLocationRequest: LocationRequest? = null
    lateinit var username: String
    private lateinit var mp: MediaPlayer
    private lateinit var myDialog: Dialog
    private var HELP_MESSAGE = "Click and hold on map to add a marker where desired\n" +
            "Click on a marker to view name and description\n" +
            "Click on name to view more information\n" +
            "Click shared reviews to view other user's reviews"
    private var local = true

    //create activity, retrieve information, and start the media player with audio file stored as raw/knit
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val extras: Bundle = intent.extras
        username = extras.getString("username")
        Log.i("string", username)
        mp = MediaPlayer.create(applicationContext, R.raw.knit)
        mp.start()
        mp.isLooping = true
    }

    /*override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }*/
    //creates the options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.add(0, Menu.FIRST+1, Menu.NONE, "Shared Reviews")
        menu.add(0, Menu.FIRST+2, Menu.NONE, "${username.capitalize()} Reviews")
        return true
    }

    //sets up option menu item actions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                myDialog = Dialog(this, R.style.PauseDialog)
                myDialog.setContentView(R.layout.help)
                myDialog.setCancelable(true)
                myDialog.show()
                val help = myDialog.findViewById<TextView>(R.id.helpText)
                help.text = HELP_MESSAGE
                help.gravity = Gravity.CENTER
                help.setPadding(10, 0, 10, 100)
                true
            }
            Menu.FIRST+1 ->{
                local = false
                googlemap!!.clear()
                displaySharedMarkers()
                true
            }
            Menu.FIRST+2 ->{
                local = true
                googlemap!!.clear()
                displayMarkers(username)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //handles googlemapapi connect event and requests user location to update map
    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this@MapsActivity)
        }
    }

    //handles onMapReady event which requests user permission to use current location in app
    //and sets up corresponding settings for the map for this
    //also sets up onmapLongClickListener to call location adder class Location and infoWindowCLickListener
    override fun onMapReady(googleMap: GoogleMap) {
        googlemap = googleMap
        googlemap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient()
                googlemap!!.isMyLocationEnabled = true
                //googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.uiSettings.isCompassEnabled = true
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
        } else {
            buildGoogleApiClient()
            googlemap!!.isMyLocationEnabled = true
            //googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
        }

        googleMap.setOnMapLongClickListener { latLng ->
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Location")
            val intent = Intent(this, com.example.dinklebot.mylocations.Location::class.java)
            intent.putExtra("values", arrayOf(username, latLng.latitude.toString(), latLng.longitude.toString()))
            mp.stop()
            startActivity(intent)
            //call activity for result here
            googleMap.addMarker(markerOptions)
            finish()
        }
        googleMap.setOnInfoWindowClickListener(this)
        displayMarkers(username)
    }

    //stop music when activity is paused
    override fun onPause() {
        super.onPause()
        mp.pause()
    }
    //resume music when activity is resumed
    override fun onResume() {
        super.onResume()
        mp.start()
    }
    //handles connection being suspended
    override fun onConnectionSuspended(p0: Int) {
        Log.e("connection Suspended", "connection Suspended")
    }
    //handles connection failing
    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e("connection Failed", "connection Failed")
    }
    //handles location of user changing to update current location marker
    override fun onLocationChanged(p0: Location?) {
        val latLng = LatLng(p0!!.latitude, p0.longitude)
        googlemap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googlemap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        }
    }

    //builds the googleApiClient for the current location information
    @Synchronized protected fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        googleApiClient!!.connect()
    }

    //requests permissions from users
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                buildGoogleApiClient()
                googlemap!!.isMyLocationEnabled = true
                googlemap!!.uiSettings.isZoomControlsEnabled = true
                googlemap!!.uiSettings.isCompassEnabled = true
            }
        } else {
            Toast.makeText(this, "Please Accept Location Permission", Toast.LENGTH_LONG).show()
        }
    }

    //function to request stored information from database to display all of current users locations
    private fun displayMarkers(name: String){
        val dbHandler = myDBHandler(this, null, null, 1)
        val dataAry = dbHandler.markerData(name)
        val len = dataAry.size
        var data: MarkerData? = null
        for(i in 0 until len){
            data = dataAry[i]
            googlemap!!.addMarker(MarkerOptions().position(
                    LatLng(data.lat!!.toDouble(), data.ln!!.toDouble()))
                    .title(data.location_name).snippet(data.description)).setTag(name)
        }
    }

    //function to display all other users reviews on the map
    //future work could allow for declaring a shared state so not all show up for someone else
    //also with too many users there could be a ton of markers on top of one another, may want to modify to contain that problem
    private fun displaySharedMarkers(){
        val dbHandler = myDBHandler(this, null, null, 1)
        val usernames = dbHandler.retrieveShared(username)
        if(usernames != null){
            for(i in 0 until usernames.size){
                displayMarkers(usernames[i])
            }
        }else{
            Toast.makeText(this, "No other reviews available", Toast.LENGTH_SHORT).show()
        }
    }

    //calls display restaurant class to show user the information about the location clicked on
    override fun onInfoWindowClick(marker: Marker?) {
        val title = marker!!.title
        val position: LatLng = marker.position
        val myDBHandler = myDBHandler(this, null, null, 1)
        val type = myDBHandler.getType(marker.tag.toString(), position.latitude.toString(), position.longitude.toString())
        //should make call to database here to see what type of location this is and call that
        //display class but I only have restaurant at this time
        var intent: Intent? = null
        if(type == "Restaurant") {
            intent = Intent(this, DisplayRest::class.java)
        }
        if(type == "Business"){
            intent = Intent(this, DisplayBusiness::class.java)
        }
        if (local) {
            intent!!.putExtra("title", arrayOf(title, username, position.latitude.toString(), position.longitude.toString(), "local"))
            startActivityForResult(intent, 1)
        } else {
            intent!!.putExtra("title", arrayOf(title, marker.tag.toString(), position.latitude.toString(), position.longitude.toString(), username))
            startActivityForResult(intent, 1)
        }
    }

    //function to handle data returned by started activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                val result= data.getStringExtra("result")
                if(result == "delete"){
                    googlemap!!.clear()
                    displayMarkers(username)
                }
            }
            /*if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }*/
        }
    }
}

