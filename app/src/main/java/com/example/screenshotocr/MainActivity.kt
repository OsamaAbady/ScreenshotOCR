package com.example.screenshotocr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var btnSelectFolder: Button
    private lateinit var btnScanScreenshots: Button
    private lateinit var btnSearch: Button
    private lateinit var etSearch: EditText
    private lateinit var tvStatus: TextView
    private lateinit var rvResults: RecyclerView
    
    private lateinit var adapter: SearchResultAdapter
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val executor = Executors.newFixedThreadPool(2)
    
    private var allResults = mutableListOf<ImageResult>()
    private var currentFolder: String? = null

    private val folderPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { handleFolderSelection(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        
        if (!checkStoragePermission()) {
            requestStoragePermission()
        }
    }

    private fun initViews() {
        btnSelectFolder = findViewById(R.id.btnSelectFolder)
        btnScanScreenshots = findViewById(R.id.btnScanScreenshots)
        btnSearch = findViewById(R.id.btnSearch)
        etSearch = findViewById(R.id.etSearch)
        tvStatus = findViewById(R.id.tvStatus)
        rvResults = findViewById(R.id.rvResults)
    }

    private fun setupRecyclerView() {
        adapter = SearchResultAdapter { imageResult ->
            // Handle item click - could open image viewer or show full text
            showImageDetails(imageResult)
        }
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = adapter
    }

    private fun setupClickListeners() {
        btnSelectFolder.setOnClickListener {
            if (checkStoragePermission()) {
                folderPickerLauncher.launch(null)
            } else {
                requestStoragePermission()
            }
        }

        btnScanScreenshots.setOnClickListener {
            if (checkStoragePermission()) {
                scanDefaultScreenshotsFolder()
            } else {
                requestStoragePermission()
            }
        }

        btnSearch.setOnClickListener {
            performSearch()
        }
    }

    private fun handleFolderSelection(uri: Uri) {
        try {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
            val path = getPathFromUri(docUri)
            if (path != null) {
                currentFolder = path
                scanFolder(path)
            } else {
                Toast.makeText(this, "Could not access selected folder", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error accessing folder: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        // This is a simplified approach - in a real app you'd need more robust URI handling
        return try {
            val path = uri.path
            if (path?.contains("/tree/primary:") == true) {
                val folder = path.substringAfter("/tree/primary:")
                "${Environment.getExternalStorageDirectory()}/$folder"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scanDefaultScreenshotsFolder() {
        val screenshotsPath = "${Environment.getExternalStorageDirectory()}/Pictures/Screenshots"
        val screenshotsFolder = File(screenshotsPath)
        
        if (screenshotsFolder.exists() && screenshotsFolder.isDirectory) {
            currentFolder = screenshotsPath
            scanFolder(screenshotsPath)
        } else {
            // Try alternative screenshot paths
            val altPaths = listOf(
                "${Environment.getExternalStorageDirectory()}/DCIM/Screenshots",
                "${Environment.getExternalStorageDirectory()}/Screenshots"
            )
            
            var found = false
            for (altPath in altPaths) {
                val altFolder = File(altPath)
                if (altFolder.exists() && altFolder.isDirectory) {
                    currentFolder = altPath
                    scanFolder(altPath)
                    found = true
                    break
                }
            }
            
            if (!found) {
                Toast.makeText(this, "Screenshots folder not found. Please select folder manually.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scanFolder(folderPath: String) {
        tvStatus.text = "Scanning folder: $folderPath"
        allResults.clear()
        adapter.updateResults(emptyList())

        executor.execute {
            val folder = File(folderPath)
            val imageFiles = folder.listFiles { file ->
                file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")
            }

            if (imageFiles.isNullOrEmpty()) {
                runOnUiThread {
                    tvStatus.text = "No images found in selected folder"
                }
                return@execute
            }

            runOnUiThread {
                tvStatus.text = "Processing ${imageFiles.size} images..."
            }

            var processed = 0
            for (imageFile in imageFiles) {
                processImageFile(imageFile) { result ->
                    processed++
                    allResults.add(result)
                    
                    runOnUiThread {
                        tvStatus.text = "Processed $processed/${imageFiles.size} images"
                        adapter.updateResults(allResults.toList())
                        
                        if (processed == imageFiles.size) {
                            tvStatus.text = "Scan complete! Found ${allResults.size} images. Use search to find specific text."
                        }
                    }
                }
            }
        }
    }

    private fun processImageFile(file: File, callback: (ImageResult) -> Unit) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                val image = InputImage.fromBitmap(bitmap, 0)
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        val result = ImageResult(
                            filePath = file.absolutePath,
                            fileName = file.name,
                            extractedText = extractedText
                        )
                        callback(result)
                    }
                    .addOnFailureListener {
                        val result = ImageResult(
                            filePath = file.absolutePath,
                            fileName = file.name,
                            extractedText = ""
                        )
                        callback(result)
                    }
            } else {
                val result = ImageResult(
                    filePath = file.absolutePath,
                    fileName = file.name,
                    extractedText = ""
                )
                callback(result)
            }
        } catch (e: Exception) {
            val result = ImageResult(
                filePath = file.absolutePath,
                fileName = file.name,
                extractedText = ""
            )
            callback(result)
        }
    }

    private fun performSearch() {
        val searchQuery = etSearch.text.toString().trim()
        if (searchQuery.isEmpty()) {
            adapter.updateResults(allResults)
            tvStatus.text = "Showing all ${allResults.size} images"
            return
        }

        val filteredResults = allResults.filter { result ->
            result.extractedText.contains(searchQuery, ignoreCase = true) ||
            result.fileName.contains(searchQuery, ignoreCase = true)
        }

        adapter.updateResults(filteredResults)
        tvStatus.text = "Found ${filteredResults.size} images matching '$searchQuery'"
    }

    private fun showImageDetails(imageResult: ImageResult) {
        // Create a simple dialog or new activity to show full image and text
        val message = """
            File: ${imageResult.fileName}
            Path: ${imageResult.filePath}
            
            Extracted Text:
            ${if (imageResult.extractedText.isNotEmpty()) imageResult.extractedText else "No text found"}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Image Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES),
            100
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Storage permission required to scan images", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}