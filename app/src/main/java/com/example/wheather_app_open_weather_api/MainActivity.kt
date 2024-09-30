package com.example.wheather_app_open_weather_api

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import android.Manifest


class MainActivity : AppCompatActivity() {
    // api id for url
    var api_key = "db435b3925bddf0584929aa6c7c666a3"

    private lateinit var btVar1: Button
    private lateinit var textView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // link the textView in which the temperature will be displayed
        textView = findViewById(R.id.textView)
        btVar1 = findViewById(R.id.btVar1)

        // create an instance of the Fused Location Provider Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // on clicking this button function to get the coordinates will be called
        btVar1.setOnClickListener {
            checkForPermission()
        }
    }

    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permissions are already granted, obtain the location
            obtainLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted, obtain the location
                obtainLocation()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation() {
        // get the last location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // check if location is not null
                if (location != null) {
                    // Create the URL for the current location
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&units=metric&appid=${api_key}"
                    // fetch the temperature with current location
                    getTemp(weatherUrl)
                } else {
                    Toast.makeText(this, "Unable to fetch current location.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getTemp(url: String) {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        // Request a string response from the provided URL.
        val stringReq = StringRequest(
            Request.Method.GET, url, { response ->
                // Get the JSON object
                val obj = JSONObject(response)

                // Getting the temperature readings from response
                val main: JSONObject = obj.getJSONObject("main")
                val temperature = main.getString("temp")
                println(temperature)

                // Getting the city name
                val city = obj.getString("name")
                println(city)

                // Set the temperature and the city name using getString() function
                textView.text = "${temperature} Градусов по цельсию в ${city}"
                System.out.println(obj.toString())
            },
            // In case of any error
            { textView.text = "That didn't work!" })

        // Add the request to the RequestQueue.
        queue.add(stringReq)
    }
}