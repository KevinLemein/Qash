package com.example.qash;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MpesaMessageAdapter extends RecyclerView.Adapter<MpesaMessageAdapter.MessageViewHolder> {

    private List<MpesaMessage> messages = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private OnMessageClickListener clickListener;

    public interface OnMessageClickListener {
        void onMessageClick(MpesaMessage message, int position);
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.clickListener = listener;
    }

    public List<MpesaMessage> getMessages() {
        return messages;
    }

    public List<MpesaMessage> getUnimportedMessages() {
        List<MpesaMessage> unimported = new ArrayList<>();
        for (MpesaMessage msg : messages) {
            if (!msg.isImported()) {
                unimported.add(msg);
            }
        }
        return unimported;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mpesa_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MpesaMessage message = messages.get(position);

        // Set message body
        holder.tvMessageBody.setText(message.getMessageBody());

        // Set date
        String date = dateFormat.format(new Date(message.getDate()));
        holder.tvMessageDate.setText(date);

        // Show/hide import status
        if (message.isImported()) {
            holder.tvImportStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvImportStatus.setVisibility(View.GONE);
        }

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null && !message.isImported()) {
                clickListener.onMessageClick(message, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<MpesaMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void markAsImported(int position) {
        if (position >= 0 && position < messages.size()) {
            messages.get(position).setImported(true);
            notifyItemChanged(position);
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody, tvMessageDate, tvImportStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tvMessageBody);
            tvMessageDate = itemView.findViewById(R.id.tvMessageDate);
            tvImportStatus = itemView.findViewById(R.id.tvImportStatus);
        }
    }
}