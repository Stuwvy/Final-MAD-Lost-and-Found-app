package com.example.back2me

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ItemListAdapter(
    private val items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.item_image)
        val itemName: TextView = view.findViewById(R.id.text_item_name)
        val itemLocation: TextView = view.findViewById(R.id.text_item_location)
        val itemTime: TextView = view.findViewById(R.id.text_item_time)
        val itemDate: TextView = view.findViewById(R.id.text_item_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost_found, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        holder.itemName.text = item.name
        holder.itemLocation.text = item.location

        // Load image if available
        if (item.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.item_card_background)
                .error(R.drawable.item_card_background)
                .into(holder.itemImage)
        } else {
            // Show default background if no image
            holder.itemImage.setBackgroundResource(R.drawable.item_card_background)
        }

        // Format time and date
        try {
            val date = isoFormat.parse(item.createdDate)
            if (date != null) {
                holder.itemTime.text = timeFormat.format(date)
                holder.itemDate.text = dateFormat.format(date)
            } else {
                holder.itemTime.text = "N/A"
                holder.itemDate.text = "N/A"
            }
        } catch (e: Exception) {
            holder.itemTime.text = "N/A"
            holder.itemDate.text = "N/A"
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size
}