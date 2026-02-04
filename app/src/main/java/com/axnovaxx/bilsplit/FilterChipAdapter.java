package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilterChipAdapter extends RecyclerView.Adapter<FilterChipAdapter.ChipViewHolder> {

    private List<String> filterOptions;
    private OnFilterSelectedListener listener;
    private int selectedPosition = 0;

    public interface OnFilterSelectedListener {
        void onFilterSelected(String filter);
    }

    public FilterChipAdapter(List<String> filterOptions, OnFilterSelectedListener listener) {
        this.filterOptions = filterOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        String filter = filterOptions.get(position);
        holder.tvFilter.setText(filter);
        
        // Update selection state
        boolean isSelected = position == selectedPosition;
        holder.itemView.setSelected(isSelected);
        
        // Change appearance based on selection
        if (isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.btn_tab_selected);
            holder.tvFilter.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.btn_tab_unselected);
            holder.tvFilter.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != position) {
                int previousPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                
                if (listener != null) {
                    listener.onFilterSelected(filter);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterOptions.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilter;

        ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilter = itemView.findViewById(R.id.tvFilter);
        }
    }
}
