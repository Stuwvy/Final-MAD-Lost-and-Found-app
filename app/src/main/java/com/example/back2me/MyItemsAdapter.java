package com.example.back2me;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MyItemsAdapter extends RecyclerView.Adapter<MyItemsAdapter.ItemViewHolder> {

    private final List<Item> items;
    private final OnItemActionListener listener;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat isoFormat;

    // Action listener interface
    public interface OnItemActionListener {
        void onItemClick(Item item);
        void onEditClick(Item item);
        void onDeleteClick(Item item);
        void onMarkResolvedClick(Item item);
    }

    public MyItemsAdapter(List<Item> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);

        // Set item name
        holder.textName.setText(item.getName());

        // Set location
        holder.textLocation.setText(item.getLocation());

        // Set status chip
        String status = item.getStatus().toLowerCase();
        switch (status) {
            case "lost":
                holder.chipStatus.setText("🔴 Lost");
                holder.chipStatus.setChipBackgroundColorResource(R.color.statusLostBackground);
                break;
            case "found":
                holder.chipStatus.setText("🟢 Found");
                holder.chipStatus.setChipBackgroundColorResource(R.color.statusFoundBackground);
                break;
            case "resolved":
                holder.chipStatus.setText("✅ Resolved");
                holder.chipStatus.setChipBackgroundColorResource(R.color.statusResolvedBackground);
                break;
            default:
                holder.chipStatus.setText(item.getStatus());
                break;
        }

        // Format date
        try {
            Date date = isoFormat.parse(item.getCreatedDate());
            if (date != null) {
                holder.textDate.setText(dateFormat.format(date));
            } else {
                holder.textDate.setText("N/A");
            }
        } catch (ParseException e) {
            holder.textDate.setText("N/A");
        }

        // Load image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.item_card_background)
                    .error(R.drawable.item_card_background)
                    .into(holder.imageItem);
        } else {
            holder.imageItem.setBackgroundResource(R.drawable.item_card_background);
        }

        // Hide mark resolved button if already resolved
        if (status.equals("resolved")) {
            holder.buttonMarkResolved.setVisibility(View.GONE);
        } else {
            holder.buttonMarkResolved.setVisibility(View.VISIBLE);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });

        holder.buttonMarkResolved.setOnClickListener(v -> {
            if (listener != null) listener.onMarkResolvedClick(item);
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
        TextView textDate;
        Chip chipStatus;
        ImageButton buttonEdit;
        ImageButton buttonDelete;
        ImageButton buttonMarkResolved;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.image_item);
            textName = itemView.findViewById(R.id.text_item_name);
            textLocation = itemView.findViewById(R.id.text_item_location);
            textDate = itemView.findViewById(R.id.text_item_date);
            chipStatus = itemView.findViewById(R.id.chip_status);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);
            buttonMarkResolved = itemView.findViewById(R.id.button_mark_resolved);
        }
    }
}