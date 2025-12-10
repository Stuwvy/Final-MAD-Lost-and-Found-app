package com.example.back2me;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    private final List<Item> items;
    private final OnItemClickListener onItemClick;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat isoFormat;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemListAdapter(List<Item> items, OnItemClickListener onItemClick) {
        this.items = items;
        this.onItemClick = onItemClick;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        this.isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        this.isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost_found, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);

        holder.itemName.setText(item.getName());
        holder.itemLocation.setText(item.getLocation());

        // Load image if available
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.item_card_background)
                    .error(R.drawable.item_card_background)
                    .into(holder.itemImage);
        } else {
            // Show default background if no image
            holder.itemImage.setBackgroundResource(R.drawable.item_card_background);
        }

        // Format time and date
        try {
            Date date = isoFormat.parse(item.getCreatedDate());
            if (date != null) {
                holder.itemTime.setText(timeFormat.format(date));
                holder.itemDate.setText(dateFormat.format(date));
            } else {
                holder.itemTime.setText("N/A");
                holder.itemDate.setText("N/A");
            }
        } catch (Exception e) {
            holder.itemTime.setText("N/A");
            holder.itemDate.setText("N/A");
        }

        holder.itemView.setOnClickListener(v -> onItemClick.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemLocation;
        TextView itemTime;
        TextView itemDate;

        ItemViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id.item_image);
            itemName = view.findViewById(R.id.text_item_name);
            itemLocation = view.findViewById(R.id.text_item_location);
            itemTime = view.findViewById(R.id.text_item_time);
            itemDate = view.findViewById(R.id.text_item_date);
        }
    }
}