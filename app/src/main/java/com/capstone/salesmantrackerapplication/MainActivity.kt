package com.capstone.salesmantrackerapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var editTextManualLocation: EditText
    private lateinit var editTextTime: EditText
    private lateinit var editTextPurpose: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonReset: Button
    private lateinit var buttonFetch: Button
    private lateinit var textViewData: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextName = findViewById(R.id.editTextName)
        editTextLocation = findViewById(R.id.editTextLocation)
        editTextTime = findViewById(R.id.editTextTime)
        editTextPurpose = findViewById(R.id.editTextPurpose)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonReset = findViewById(R.id.buttonReset)
        buttonFetch = findViewById(R.id.buttonFetch)
        textViewData = findViewById(R.id.textViewData)
        editTextManualLocation = findViewById(R.id.editTextManualLocation)

        auth = FirebaseAuth.getInstance()
        dbHelper = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        dbRef = FirebaseDatabase.getInstance().getReference("Tracks")

        buttonSubmit.setOnClickListener { submitData() }
        buttonReset.setOnClickListener { resetFields() }
        buttonFetch.setOnClickListener { fetchData() }

        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndTime()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun submitData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Please log in before submitting data.", Toast.LENGTH_LONG).show()
            return
        }

        val userId = user.uid
        val name = editTextName.text.toString().trim()
        val autoFetchedLocation = editTextLocation.text.toString().trim()
        val time = editTextTime.text.toString().trim()
        val purpose = editTextPurpose.text.toString().trim()
        val manualLocation = editTextManualLocation.text.toString().trim()
        val trackId = dbRef.child(userId).push().key!!

        if (name.isEmpty() || manualLocation.isEmpty() || autoFetchedLocation.isEmpty() || time.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields before submitting.", Toast.LENGTH_LONG).show()
            return
        }

        val result = dbHelper.insertData(name, manualLocation, autoFetchedLocation, time, purpose)

        if (result != -1L) {
            Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Error saving data.", Toast.LENGTH_LONG).show()
        }

        val track = Track(trackId, name, autoFetchedLocation, time, purpose, manualLocation)
        dbRef.child(userId).child(trackId).setValue(track).addOnCompleteListener{
            Toast.makeText(this,"Data Uploaded Successfully!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener{ err ->
            Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun resetFields() {
        editTextName.text.clear()
        editTextLocation.text.clear()
        editTextTime.text.clear()
        editTextPurpose.text.clear()
        editTextManualLocation.text.clear()

        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        editTextTime.setText(currentTime)

        fetchCurrentTimeAndLocation()
    }

    private fun fetchData() {
        val cursor = dbHelper.getAllData()
        val dataBuilder = StringBuilder()

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val manualLocation = cursor.getString(cursor.getColumnIndex("manual_location"))
                val autoLocation = cursor.getString(cursor.getColumnIndex("auto_location"))
                val time = cursor.getString(cursor.getColumnIndex("time"))
                val purpose = cursor.getString(cursor.getColumnIndex("purpose"))

                dataBuilder.append("Name: $name\n")
                dataBuilder.append("Manual Location: $manualLocation\n")
                dataBuilder.append("Auto-fetched Location: $autoLocation\n")
                dataBuilder.append("Time: $time\n")
                dataBuilder.append("Purpose: $purpose\n\n")
            } while (cursor.moveToNext())
            Log.d("FetchData", "Fetched Data: $dataBuilder")
        } else {
            dataBuilder.append("No data found.")
            Log.d("FetchData", "No data found in the database.")
        }
        cursor.close()

        val intent = Intent(this, DataDisplayActivity::class.java).apply {
            putExtra("fetchedData", dataBuilder.toString())
        }
        startActivity(intent)
    }

    private fun fetchLocationAndTime() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLocation = "Lat: ${it.latitude}, Lon: ${it.longitude}"
                editTextLocation.setText(currentLocation)

                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                editTextTime.setText(currentTime)
            }
        }
    }

    private fun fetchCurrentTimeAndLocation() {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        editTextTime.setText(currentTime)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                editTextLocation.setText("Lat: $latitude, Long: $longitude")
            } else {
                Toast.makeText(this, "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Location request failed.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndTime()
        } else {
            Toast.makeText(this, "Location permission is required to fetch location and time", Toast.LENGTH_LONG).show()
        }
    }
}
