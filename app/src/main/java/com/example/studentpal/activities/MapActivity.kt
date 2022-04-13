package com.example.studentpal.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityMapBinding
import com.example.studentpal.models.Board
import com.example.studentpal.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapActivity : BaseActivity(), OnMapReadyCallback {
    private var eventDetail: Board? = null //Global variable
    private var binding : ActivityMapBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            eventDetail = intent.getParcelableExtra(Constants.BOARD_DETAIL)
        }

        if (eventDetail != null){
            setupActionBar()

            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

            supportMapFragment.getMapAsync(this)
        }
    }

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

    override fun onMapReady(googleMap: GoogleMap) {
        // Gets the Earth location of the event
        val position = LatLng(eventDetail!!.latitude, eventDetail!!.longitude)
        // Adds a marker to the respective latitude and longitude location
        googleMap.addMarker(MarkerOptions().position(position).title(eventDetail!!.eventLocation))
        // Map zooms in on the location marker by a float value of 15
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        // Animates the map view to automatically zoom in to the location
        googleMap.animateCamera(newLatLngZoom)
    }
}