package com.example.screenshotocr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screenshotocr.adapter.SearchResultsAdapter
import com.example.screenshotocr.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: SearchResultsAdapter
    private lateinit var db: AppDatabase
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Check permissions first
        if (!hasMediaPermission()) {
            requestMediaPermission()
            return
        }

        initializeViews()
    }

    private fun hasMediaPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMediaPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeViews()
            } else {
                Toast.makeText(
                    this,
                    "Media permission is required to search screenshots",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun initializeViews() {
        try {
            db = AppDatabase.get(this)
            
            // Setup RecyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.recycler)
            adapter = SearchResultsAdapter(this)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            // Setup SearchView
            val search = findViewById<SearchView>(R.id.search)
            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    performSearch(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.length >= 2) {
                        performSearch(newText)
                    } else if (newText.isEmpty()) {
                        adapter.updateResults(emptyList())
                    }
                    return true
                }
            })

            // Load all screenshots initially
            loadAllScreenshots()
            
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error initializing views", e)
            Toast.makeText(this, "Error initializing search", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            loadAllScreenshots()
            return
        }

        lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    db.dao().search(query)
                }
                
                withContext(Dispatchers.Main) {
                    adapter.updateResults(results)
                    Log.d("SearchActivity", "Found ${results.size} results for query: $query")
                }
            } catch (e: Exception) {
                Log.e("SearchActivity", "Error performing search", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SearchActivity,
                        "Search failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadAllScreenshots() {
        lifecycleScope.launch {
            try {
                val allScreenshots = withContext(Dispatchers.IO) {
                    db.dao().getAllScreenshots()
                }
                
                withContext(Dispatchers.Main) {
                    adapter.updateResults(allScreenshots)
                    Log.d("SearchActivity", "Loaded ${allScreenshots.size} screenshots")
                }
            } catch (e: Exception) {
                Log.e("SearchActivity", "Error loading screenshots", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SearchActivity,
                        "Failed to load screenshots",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}