package com.example.screenshotocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var btnScan: Button
    private lateinit var btnGallery: Button
    private lateinit var tvResult: TextView
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processImage(it) }
    }
    
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { processImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan = findViewById(R.id.btnScan)
        btnGallery = findViewById(R.id.btnGallery)
        tvResult = findViewById(R.id.tvResult)

        btnScan.setOnClickListener {
            if (checkCameraPermission()) {
                cameraLauncher.launch(null)
            } else {
                requestCameraPermission()
            }
        }

        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
    }
    
    private fun processImage(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            processImageWithMLKit(image)
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        processImageWithMLKit(image)
    }
    
    private fun processImageWithMLKit(image: InputImage) {
        tvResult.text = "Processing image..."
        
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isNotEmpty()) {
                    tvResult.text = "Extracted Text:\n\n$extractedText"
                } else {
                    tvResult.text = "No text found in image"
                }
            }
            .addOnFailureListener { e ->
                tvResult.text = "OCR failed: ${e.message}"
                Toast.makeText(this, "OCR processing failed", Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(this, "Camera permission required for taking photos", Toast.LENGTH_SHORT).show()
        }
    }
}