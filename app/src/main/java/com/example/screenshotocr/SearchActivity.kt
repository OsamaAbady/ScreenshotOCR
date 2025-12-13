package com.example.screenshotocr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.SearchView
import com.example.screenshotocr.db.AppDatabase

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val db = AppDatabase.get(this)
        val search = findViewById<SearchView>(R.id.search)

        search.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String): Boolean {
                db.dao().search(q)
                return true
            }
            override fun onQueryTextChange(q: String) = false
        })
    }
}