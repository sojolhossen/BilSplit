package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axnovaxx.bilsplit.ItemizedResultActivity.SplitResultItem;

import java.text.DecimalFormat;
import java.util.List;

public class SplitResultAdapter extends RecyclerView.Adapter<SplitResultAdapter.SplitResultViewHolder> {

    private List<SplitResultItem> splitResultItems;
    private DecimalFormat currencyFormat;

    public SplitResultAdapter(List<SplitResultItem> splitResultItems, DecimalFormat currencyFormat) {
        this.splitResultItems = splitResultItems;
        this.currencyFormat = currencyFormat;
    }

    @NonNull
    @Override
    public SplitResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_split_result, parent, false);
        return new SplitResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitResultViewHolder holder, int position) {
        SplitResultItem item = splitResultItems.get(position);
        
        holder.tvPersonName.setText(item.getPersonName());
        holder.tvPersonAmount.setText("$" + currencyFormat.format(item.getAmount()));
        holder.tvItemCount.setText(item.getItemCount() + " items");
        holder.tvPersonPercentage.setText(String.format("%.1f%%", item.getPercentage()));
        
        // No need for avatar or color changes in receipt style
        // Keep simple monospace font style
    }

    @Override
    public int getItemCount() {
        return splitResultItems.size();
    }

    static class SplitResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvPersonName, tvPersonAmount, tvItemCount, tvPersonPercentage;

        SplitResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            tvPersonAmount = itemView.findViewById(R.id.tvPersonAmount);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvPersonPercentage = itemView.findViewById(R.id.tvPersonPercentage);
        }
    }
}
