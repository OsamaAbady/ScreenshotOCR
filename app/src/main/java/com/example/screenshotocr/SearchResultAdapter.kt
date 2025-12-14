package com.example.screenshotocr

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SearchResultAdapter(
    private var results: List<ImageResult> = emptyList(),
    private val onItemClick: (ImageResult) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val tvExtractedText: TextView = view.findViewById(R.id.tvExtractedText)
        val tvFilePath: TextView = view.findViewById(R.id.tvFilePath)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        
        holder.tvFileName.text = result.fileName
        holder.tvExtractedText.text = if (result.extractedText.isNotEmpty()) {
            result.extractedText
        } else {
            "No text found in this image"
        }
        holder.tvFilePath.text = File(result.filePath).parent ?: ""

        // Load thumbnail
        try {
            val bitmap = BitmapFactory.decodeFile(result.filePath)
            if (bitmap != null) {
                holder.ivThumbnail.setImageBitmap(bitmap)
            } else {
                holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } catch (e: Exception) {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onItemClick(result)
        }
    }

    override fun getItemCount() = results.size

    fun updateResults(newResults: List<ImageResult>) {
        results = newResults
        notifyDataSetChanged()
    }
}