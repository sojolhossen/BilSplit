package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<SplitHistory> historyList;
    private OnHistoryClickListener clickListener;
    private OnHistoryDeleteListener deleteListener;
    private DecimalFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public interface OnHistoryClickListener {
        void onHistoryClick(SplitHistory history);
    }

    public interface OnHistoryDeleteListener {
        void onHistoryDelete(SplitHistory history);
    }

    public HistoryAdapter(List<SplitHistory> historyList, OnHistoryClickListener clickListener, OnHistoryDeleteListener deleteListener) {
        this.historyList = historyList != null ? historyList : new ArrayList<>();
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
        this.currencyFormat = new DecimalFormat("#0.00");
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SplitHistory history = historyList.get(position);
        
        holder.tvAmount.setText("$" + currencyFormat.format(history.getFinalAmount()));
        holder.tvDate.setText(formatDate(history.getCreatedAt()));
        holder.tvPeopleCount.setText(history.getPeopleCount() + " people");
        holder.tvSplitType.setText(getSplitTypeDisplay(history.getSplitType()));
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onHistoryClick(history);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onHistoryDelete(history);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateData(List<SplitHistory> newHistoryList) {
        this.historyList = newHistoryList != null ? newHistoryList : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateStr);
            return dateFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String getSplitTypeDisplay(String splitType) {
        switch (splitType) {
            case "regular":
                return "Equal Split";
            case "custom":
                return "Custom Split";
            case "itemized":
                return "Itemized Split";
            default:
                return "Split";
        }
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvDate, tvPeopleCount, tvSplitType;
        ImageView btnDelete;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPeopleCount = itemView.findViewById(R.id.tvPeopleCount);
            tvSplitType = itemView.findViewById(R.id.tvSplitType);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
