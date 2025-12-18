package com.example.back2me;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MyItemsAdapter extends RecyclerView.Adapter<MyItemsAdapter.ViewHolder> {

    private List<Item> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onItemClick(Item item);
        void onEditClick(Item item);
        void onDeleteClick(Item item);
        void onViewClaimsClick(Item item);
        void onMarkResolvedClick(Item item);
    }

    public MyItemsAdapter(List<Item> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageItem;
        TextView textName;
        TextView textLocation;
        TextView textDate;
        TextView textStatus;
        ImageView buttonMenu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_item);
            imageItem = itemView.findViewById(R.id.image_item);
            textName = itemView.findViewById(R.id.text_item_name);
            textLocation = itemView.findViewById(R.id.text_location);
            textDate = itemView.findViewById(R.id.text_date);
            textStatus = itemView.findViewById(R.id.text_status);
            buttonMenu = itemView.findViewById(R.id.button_menu);
        }

        void bind(Item item) {
            textName.setText(item.getName());
            textLocation.setText(item.getLocation());
            textDate.setText(formatDate(item.getCreatedDate()));

            // Status badge
            String status = item.getStatus();
            if ("lost".equalsIgnoreCase(status)) {
                textStatus.setText(R.string.lost);
                textStatus.setBackgroundResource(R.drawable.badge_lost);
            } else if ("found".equalsIgnoreCase(status)) {
                textStatus.setText(R.string.found);
                textStatus.setBackgroundResource(R.drawable.badge_found);
            } else {
                textStatus.setText(R.string.resolved);
                textStatus.setBackgroundResource(R.drawable.badge_resolved);
            }

            // Image
            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(imageItem);
            } else {
                imageItem.setImageResource(R.drawable.placeholder_image);
            }

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            buttonMenu.setOnClickListener(v -> showPopupMenu(v, item));
        }

        private void showPopupMenu(View anchor, Item item) {
            PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
            popup.getMenuInflater().inflate(R.menu.menu_my_item, popup.getMenu());

            // Hide "Mark Resolved" if already resolved
            if ("resolved".equalsIgnoreCase(item.getStatus())) {
                popup.getMenu().findItem(R.id.action_mark_resolved).setVisible(false);
            }

            popup.setOnMenuItemClickListener(menuItem -> {
                if (listener == null) return false;

                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_edit) {
                    listener.onEditClick(item);
                    return true;
                } else if (itemId == R.id.action_view_claims) {
                    listener.onViewClaimsClick(item);
                    return true;
                } else if (itemId == R.id.action_mark_resolved) {
                    listener.onMarkResolvedClick(item);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    listener.onDeleteClick(item);
                    return true;
                }
                return false;
            });

            popup.show();
        }

        private String formatDate(String isoDate) {
            if (isoDate == null || isoDate.isEmpty()) return "";

            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoFormat.parse(isoDate);

                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                return displayFormat.format(date);
            } catch (ParseException e) {
                return isoDate;
            }
        }
    }
}