package com.example.back2me;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    private final List<Item> items;
    private final OnItemClickListener onItemClickListener;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final SimpleDateFormat isoFormat;

    // Click listener interface
    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemListAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.onItemClickListener = listener;

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        } catch (ParseException e) {
            holder.itemTime.setText("N/A");
            holder.itemDate.setText("N/A");
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemLocation;
        TextView itemTime;
        TextView itemDate;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.text_item_name);
            itemLocation = itemView.findViewById(R.id.text_item_location);
            itemTime = itemView.findViewById(R.id.text_item_time);
            itemDate = itemView.findViewById(R.id.text_item_date);
        }
    }
}