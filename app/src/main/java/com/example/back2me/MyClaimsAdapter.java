package com.example.back2me;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MyClaimsAdapter extends RecyclerView.Adapter<MyClaimsAdapter.ClaimViewHolder> {

    private final List<Claim> claims;
    private final OnClaimClickListener listener;

    private final SimpleDateFormat isoFormat;
    private final SimpleDateFormat displayFormat;

    public interface OnClaimClickListener {
        void onClaimClick(Claim claim);
    }

    public MyClaimsAdapter(List<Claim> claims, OnClaimClickListener listener) {
        this.claims = claims;
        this.listener = listener;

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        displayFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_claim, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        Claim claim = claims.get(position);

        // Item name
        holder.textItemName.setText(claim.getItemName());

        // Claim message
        holder.textMessage.setText(claim.getMessage());

        // Status with color
        String status = claim.getStatus();
        holder.textStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
        
        switch (status.toLowerCase()) {
            case "approved":
                holder.textStatus.setBackgroundResource(R.drawable.badge_found);
                break;
            case "rejected":
                holder.textStatus.setBackgroundResource(R.drawable.badge_lost);
                break;
            default: // pending
                holder.textStatus.setBackgroundResource(R.drawable.badge_pending);
                break;
        }

        // Item status (lost/found)
        String itemStatus = claim.getItemStatus();
        if (itemStatus != null && itemStatus.equalsIgnoreCase("lost")) {
            holder.textItemStatus.setText(R.string.status_lost);
            holder.textItemStatus.setBackgroundResource(R.drawable.badge_lost);
        } else {
            holder.textItemStatus.setText(R.string.status_found);
            holder.textItemStatus.setBackgroundResource(R.drawable.badge_found);
        }

        // Date
        try {
            Date date = isoFormat.parse(claim.getCreatedDate());
            if (date != null) {
                holder.textDate.setText(displayFormat.format(date));
            }
        } catch (ParseException e) {
            holder.textDate.setText(claim.getCreatedDate());
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClaimClick(claim);
            }
        });
    }

    @Override
    public int getItemCount() {
        return claims.size();
    }

    static class ClaimViewHolder extends RecyclerView.ViewHolder {
        TextView textItemName;
        TextView textMessage;
        TextView textStatus;
        TextView textItemStatus;
        TextView textDate;

        ClaimViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.text_item_name);
            textMessage = itemView.findViewById(R.id.text_message);
            textStatus = itemView.findViewById(R.id.text_status);
            textItemStatus = itemView.findViewById(R.id.text_item_status);
            textDate = itemView.findViewById(R.id.text_date);
        }
    }
}
