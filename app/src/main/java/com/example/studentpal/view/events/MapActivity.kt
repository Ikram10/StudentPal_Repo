package com.example.studentpal.view.events

import android.os.Bundle
import com.example.studentpal.R
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.databinding.ActivityMapBinding
import com.example.studentpal.model.entities.Event
import com.example.studentpal.common.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
/**
 * This activity is responsible for displaying a map of the event's location to the user.
 *
 * The author used the [Maps SDK documentation](https://developers.google.com/maps/documentation/android-sdk/overview)
 * to understand the content and apply it to the project.
 *
 * The code displayed was reused from Denis Panjuta's Trello clone (see references file)
 *
 * @see[com.example.studentpal.common.References]
 */

class MapActivity : BaseActivity(), OnMapReadyCallback {
    //Global variables
    private var eventDetail: Event? = null
    private var binding : ActivityMapBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // retrieves the event details from the intent
        if (intent.hasExtra(Constants.EVENT_DETAIL)) {
            eventDetail = intent.getParcelableExtra(Constants.EVENT_DETAIL)
        }

        if (eventDetail != null){
            setupActionBar()

            //initialise the map fragment
            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

            supportMapFragment.getMapAsync(this)
        }
    }

    /**
     * method sets up the Action bar
     */
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarMap)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Event: ${eventDetail?.name}"
        }
        binding?.toolbarMap?.setNavigationOnClickListener{
            onBackPressed()
        }

    }

    /**
     * Callback interface for when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        // Gets the Earth location of the event
        val position =
            LatLng(eventDetail!!
                .latitude, eventDetail!!.longitude)
        // Adds a marker to the respective latitude and longitude location
        googleMap
            .addMarker(MarkerOptions()
                .position(position)
                .title(eventDetail!!
                    .eventLocation))
        // Map zooms in on the location marker by a float value of 15
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        // Animates the map view to automatically zoom in to the location
        googleMap.animateCamera(newLatLngZoom)
    }
}