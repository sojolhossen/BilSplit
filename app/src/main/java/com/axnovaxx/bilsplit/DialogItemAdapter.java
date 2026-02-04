package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class DialogItemAdapter extends RecyclerView.Adapter<DialogItemAdapter.DialogItemViewHolder> {

    private List<SplitItem> itemsList;
    private DecimalFormat currencyFormat;

    public DialogItemAdapter(List<SplitItem> itemsList) {
        this.itemsList = itemsList;
        this.currencyFormat = new DecimalFormat("#0.00");
    }

    @NonNull
    @Override
    public DialogItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog_expense_item, parent, false);
        return new DialogItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogItemViewHolder holder, int position) {
        SplitItem item = itemsList.get(position);
        
        holder.tvItemName.setText(item.getName());
        holder.tvItemDetails.setText(String.format("%dx $%.2f", item.getQuantity(), item.getPrice()));
        holder.tvItemAmount.setText(String.format("$%.2f", item.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    static class DialogItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDetails, tvItemAmount;

        DialogItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDetails = itemView.findViewById(R.id.tvItemDetails);
            tvItemAmount = itemView.findViewById(R.id.tvItemAmount);
        }
    }
}
