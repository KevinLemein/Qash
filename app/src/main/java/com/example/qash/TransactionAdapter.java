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

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private OnTransactionLongClickListener longClickListener;

    // Interface for long-click callback
    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(Transaction transaction);
    }

    // Set listener
    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set description
        holder.tvDescription.setText(transaction.getDescription());

        // Set category
        holder.tvCategory.setText(transaction.getCategory());

        // Set date
        String date = dateFormat.format(new Date(transaction.getDate()));
        holder.tvDate.setText(date);

        // Set amount with sign and color
        String sign = transaction.getType().equals("Expense") ? "- " : "+ ";
        String amount = String.format(Locale.getDefault(), "%sKES %.2f", sign, transaction.getAmount());
        holder.tvAmount.setText(amount);

        // Color: Red for expenses, Green for income
        int color = transaction.getType().equals("Expense") ? 0xFFF44336 : 0xFF4CAF50;
        holder.tvAmount.setTextColor(color);

        // Set category icon
        String icon = getCategoryIcon(transaction.getCategory());
        holder.tvCategoryIcon.setText(icon);

        // Handle long-click
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTransactionLongClick(transaction);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    private String getCategoryIcon(String category) {
        switch (category) {
            case "Transport":
                return "🚕";
            case "Food & Groceries":
                return "🛒";
            case "Bills & Utilities":
                return "💡";
            case "Entertainment":
                return "🎬";
            case "Airtime & Data":
                return "📱";
            case "Shopping":
                return "🛍️";
            case "Health":
                return "🏥";
            default:
                return "📦";
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvDescription, tvCategory, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}