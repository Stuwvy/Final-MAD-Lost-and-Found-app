package com.example.back2me;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ClaimsAdapter extends RecyclerView.Adapter<ClaimsAdapter.ClaimViewHolder> {

    private final List<Claim> claims;
    private final OnClaimActionListener listener;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault());
    private final SimpleDateFormat isoFormat;

    // Action listener interface
    public interface OnClaimActionListener {
        void onApproveClick(Claim claim);
        void onRejectClick(Claim claim);
        void onContactClick(Claim claim);
    }

    public ClaimsAdapter(List<Claim> claims, OnClaimActionListener listener) {
        this.claims = claims;
        this.listener = listener;

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_claim, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        Claim claim = claims.get(position);

        // Set claimer name
        holder.textClaimerName.setText(claim.getClaimerName());

        // Set claimer email
        holder.textClaimerEmail.setText(claim.getClaimerEmail());

        // Set claim message
        holder.textMessage.setText(claim.getMessage());

        // Set claim type based on item status
        if ("lost".equals(claim.getItemStatus())) {
            holder.textClaimType.setText("🔍 Says they found it");
        } else {
            holder.textClaimType.setText("🙋 Claims ownership");
        }

        // Set status chip
        String status = claim.getStatus().toLowerCase();
        switch (status) {
            case "pending":
                holder.chipStatus.setText("⏳ Pending");
                holder.chipStatus.setChipBackgroundColorResource(R.color.statusPendingBackground);
                holder.layoutActions.setVisibility(View.VISIBLE);
                break;
            case "approved":
                holder.chipStatus.setText("✅ Approved");
                holder.chipStatus.setChipBackgroundColorResource(R.color.statusFoundBackground);
                holder.layoutActions.setVisibility(View.GONE);
                break;
            case "rejected":
                holder.chipStatus.setText("❌ Rejected");
                holder.chipStatus.setChipBackgroundColorResource(R.color.statusLostBackground);
                holder.layoutActions.setVisibility(View.GONE);
                break;
            default:
                holder.chipStatus.setText(claim.getStatus());
                break;
        }

        // Format date
        try {
            Date date = isoFormat.parse(claim.getCreatedDate());
            if (date != null) {
                holder.textDate.setText(dateFormat.format(date));
            } else {
                holder.textDate.setText("N/A");
            }
        } catch (ParseException e) {
            holder.textDate.setText("N/A");
        }

        // Click listeners
        holder.buttonApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApproveClick(claim);
        });

        holder.buttonReject.setOnClickListener(v -> {
            if (listener != null) listener.onRejectClick(claim);
        });

        holder.buttonContact.setOnClickListener(v -> {
            if (listener != null) listener.onContactClick(claim);
        });
    }

    @Override
    public int getItemCount() {
        return claims.size();
    }

    public static class ClaimViewHolder extends RecyclerView.ViewHolder {
        TextView textClaimerName;
        TextView textClaimerEmail;
        TextView textClaimType;
        TextView textMessage;
        TextView textDate;
        Chip chipStatus;
        LinearLayout layoutActions;
        MaterialButton buttonApprove;
        MaterialButton buttonReject;
        ImageButton buttonContact;

        public ClaimViewHolder(@NonNull View itemView) {
            super(itemView);
            textClaimerName = itemView.findViewById(R.id.text_claimer_name);
            textClaimerEmail = itemView.findViewById(R.id.text_claimer_email);
            textClaimType = itemView.findViewById(R.id.text_claim_type);
            textMessage = itemView.findViewById(R.id.text_message);
            textDate = itemView.findViewById(R.id.text_date);
            chipStatus = itemView.findViewById(R.id.chip_status);
            layoutActions = itemView.findViewById(R.id.layout_actions);
            buttonApprove = itemView.findViewById(R.id.button_approve);
            buttonReject = itemView.findViewById(R.id.button_reject);
            buttonContact = itemView.findViewById(R.id.button_contact);
        }
    }
}
