package com.capstone.salesmantrackerapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DataDisplayActivity : AppCompatActivity() {

    private lateinit var textViewDataDisplay: TextView
    private lateinit var buttonClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)

        textViewDataDisplay = findViewById(R.id.textViewDataDisplay)
        buttonClose = findViewById(R.id.buttonClose)

        val fetchedData = intent.getStringExtra("fetchedData")
        textViewDataDisplay.text = fetchedData ?: "No data to display."

        buttonClose.setOnClickListener {
            finish()
        }
    }
}
