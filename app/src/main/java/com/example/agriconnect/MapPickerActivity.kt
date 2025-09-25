package com.example.agriconnect

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var pickedMarker: Marker? = null
    private var pickedLatLng: LatLng? = null
    private var pickedAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        // Load Map Fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Confirm button
        val btnConfirm: Button = findViewById(R.id.btnConfirmLocation)
        btnConfirm.setOnClickListener {
            if (pickedLatLng != null) {
                val resultIntent = Intent().apply {
                    putExtra("latitude", pickedLatLng?.latitude)
                    putExtra("longitude", pickedLatLng?.longitude)
                    putExtra("address", pickedAddress)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Default position (Philippines / Cebu City for example)
        val defaultLatLng = LatLng(10.3157, 123.8854)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 12f))

        // Long press to select a location
        mMap.setOnMapLongClickListener { latLng ->
            pickedLatLng = latLng

            // Remove old marker
            pickedMarker?.remove()

            // Add new marker
            pickedMarker = mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Get address name
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                pickedAddress = addresses[0].getAddressLine(0)
                pickedMarker?.title = pickedAddress
                pickedMarker?.showInfoWindow()
            } else {
                pickedAddress = "Unnamed Location"
            }
        }
    }
}
