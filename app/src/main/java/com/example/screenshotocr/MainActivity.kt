package com.example.screenshotocr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btnScan)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        btnScan.setOnClickListener {
            Toast.makeText(this, "OCR Scan feature - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        btnSearch.setOnClickListener {
            Toast.makeText(this, "Search feature - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
}