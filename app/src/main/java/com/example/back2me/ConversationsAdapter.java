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
import java.util.concurrent.TimeUnit;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private final List<Conversation> conversations;
    private final String currentUserId;
    private final OnConversationClickListener listener;

    private final SimpleDateFormat isoFormat;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationsAdapter(List<Conversation> conversations, String currentUserId,
                                 OnConversationClickListener listener) {
        this.conversations = conversations;
        this.currentUserId = currentUserId;
        this.listener = listener;

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        // Set other participant's name
        String otherName = conversation.getOtherParticipantName(currentUserId);
        holder.textUserName.setText(otherName);

        // Set avatar initial
        if (otherName != null && !otherName.isEmpty()) {
            holder.textAvatar.setText(String.valueOf(otherName.charAt(0)).toUpperCase());
        } else {
            holder.textAvatar.setText("?");
        }

        // Set item name if available
        String itemName = conversation.getItemName();
        if (itemName != null && !itemName.isEmpty()) {
            holder.textItemName.setText(itemName);
            holder.textItemName.setVisibility(View.VISIBLE);
        } else {
            holder.textItemName.setVisibility(View.GONE);
        }

        // Set last message
        String lastMessage = conversation.getLastMessage();
        if (lastMessage != null && !lastMessage.isEmpty()) {
            // Add "You: " prefix if current user sent the last message
            if (currentUserId.equals(conversation.getLastSenderId())) {
                holder.textLastMessage.setText(holder.itemView.getContext()
                        .getString(R.string.you_prefix, lastMessage));
            } else {
                holder.textLastMessage.setText(lastMessage);
            }
        } else {
            holder.textLastMessage.setText(R.string.no_messages_yet);
        }

        // Set timestamp
        holder.textTime.setText(formatTimestamp(conversation.getLastMessageTime()));

        // Show unread indicator
        int unreadCount = conversation.getUnreadCount();
        if (unreadCount > 0 && !currentUserId.equals(conversation.getLastSenderId())) {
            holder.viewUnreadIndicator.setVisibility(View.VISIBLE);
            holder.textLastMessage.setAlpha(1.0f);
        } else {
            holder.viewUnreadIndicator.setVisibility(View.GONE);
            holder.textLastMessage.setAlpha(0.7f);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {
            Date date = isoFormat.parse(timestamp);
            if (date == null) return "";

            Date now = new Date();
            long diffMs = now.getTime() - date.getTime();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMs);

            if (diffDays == 0) {
                // Today - show time
                return timeFormat.format(date);
            } else if (diffDays < 7) {
                // This week - show day name
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                return dayFormat.format(date);
            } else {
                // Older - show date
                return dateFormat.format(date);
            }
        } catch (ParseException e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView textAvatar;
        TextView textUserName;
        TextView textItemName;
        TextView textLastMessage;
        TextView textTime;
        View viewUnreadIndicator;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            textAvatar = itemView.findViewById(R.id.text_avatar);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textItemName = itemView.findViewById(R.id.text_item_name);
            textLastMessage = itemView.findViewById(R.id.text_last_message);
            textTime = itemView.findViewById(R.id.text_time);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);
        }
    }
}
