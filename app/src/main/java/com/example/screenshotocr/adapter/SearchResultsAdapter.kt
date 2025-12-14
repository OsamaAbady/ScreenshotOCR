package com.example.screenshotocr.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.screenshotocr.R
import com.example.screenshotocr.db.ScreenshotEntity
import java.io.File

class SearchResultsAdapter(
    private val context: Context,
    private var screenshots: List<ScreenshotEntity> = emptyList()
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.screenshot_image)
        val textView: TextView = view.findViewById(R.id.screenshot_text)
        val pathView: TextView = view.findViewById(R.id.screenshot_path)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_screenshot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screenshot = screenshots[position]
        
        // Load image with Glide
        val file = File(screenshot.path)
        if (file.exists()) {
            Glide.with(context)
                .load(file)
                .centerCrop()
                .into(holder.imageView)
        } else {
            // Use a simple placeholder if image not found
            holder.imageView.setImageDrawable(null)
        }
        
        // Set text content (truncate if too long)
        val displayText = if (screenshot.text.length > 200) {
            screenshot.text.take(200) + "..."
        } else {
            screenshot.text
        }
        holder.textView.text = displayText
        
        // Set file path
        holder.pathView.text = File(screenshot.path).name
    }

    override fun getItemCount(): Int = screenshots.size

    fun updateResults(newScreenshots: List<ScreenshotEntity>) {
        screenshots = newScreenshots
        notifyDataSetChanged()
    }
}