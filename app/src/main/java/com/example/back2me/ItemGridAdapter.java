package com.example.back2me;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class ItemGridAdapter extends RecyclerView.Adapter<ItemGridAdapter.ItemViewHolder> {

    private final List<Item> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemGridAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost_found_grid, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);

        // Set item name
        holder.textName.setText(item.getName());

        // Set location
        holder.textLocation.setText(item.getLocation());

        // Set status badge
        String status = item.getStatus();
        if (status != null) {
            if (status.equalsIgnoreCase("lost")) {
                holder.textStatus.setText(R.string.status_lost);
                holder.textStatus.setBackgroundResource(R.drawable.badge_lost);
            } else if (status.equalsIgnoreCase("found")) {
                holder.textStatus.setText(R.string.status_found);
                holder.textStatus.setBackgroundResource(R.drawable.badge_found);
            } else if (status.equalsIgnoreCase("resolved")) {
                holder.textStatus.setText(R.string.status_resolved);
                holder.textStatus.setBackgroundResource(R.drawable.badge_resolved);
            }
        }

        // Load image with Glide
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imageItem);
        } else {
            holder.imageItem.setImageResource(R.drawable.placeholder_image);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageItem;
        TextView textName;
        TextView textLocation;
        TextView textStatus;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.image_item);
            textName = itemView.findViewById(R.id.text_item_name);
            textLocation = itemView.findViewById(R.id.text_item_location);
            textStatus = itemView.findViewById(R.id.text_item_status);
        }
    }
}