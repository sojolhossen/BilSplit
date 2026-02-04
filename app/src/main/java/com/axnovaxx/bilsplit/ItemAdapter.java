package com.axnovaxx.bilsplit;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axnovaxx.bilsplit.ItemizedSplitActivity.ItemizedItem;
import com.axnovaxx.bilsplit.ItemizedSplitActivity.Person;

import java.text.DecimalFormat;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<ItemizedItem> itemsList;
    private List<Person> peopleList;
    private OnItemDeleteListener deleteListener;
    private OnItemChangeListener changeListener;
    private DecimalFormat currencyFormat;

    public interface OnItemDeleteListener {
        void onItemDelete(int position);
    }

    public interface OnItemChangeListener {
        void onItemChanged();
    }

    public ItemAdapter(List<ItemizedItem> itemsList, List<Person> peopleList, 
                      OnItemDeleteListener deleteListener, OnItemChangeListener changeListener) {
        this.itemsList = itemsList;
        this.peopleList = peopleList;
        this.deleteListener = deleteListener;
        this.changeListener = changeListener;
        this.currencyFormat = new DecimalFormat("#0.00");
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_itemized_split, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemizedItem item = itemsList.get(position);
        
        holder.etItemName.setText(item.getName());
        holder.etItemPrice.setText(String.valueOf(item.getPrice()));
        holder.etItemQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvItemSubtotal.setText("$" + currencyFormat.format(item.getSubtotal()));
        
        // Setup people selection RecyclerView
        PeopleSelectionAdapter selectionAdapter = new PeopleSelectionAdapter(peopleList, item.getSelectedPeople(), changeListener);
        holder.rvPeopleSelection.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.rvPeopleSelection.setAdapter(selectionAdapter);
        
        // Setup text watchers for real-time updates
        holder.etItemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                item.setName(s.toString());
                if (changeListener != null) {
                    changeListener.onItemChanged();
                }
            }
        });
        
        holder.etItemPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double price = Double.parseDouble(s.toString());
                    item.setPrice(price);
                    holder.tvItemSubtotal.setText("$" + currencyFormat.format(item.getSubtotal()));
                    if (changeListener != null) {
                        changeListener.onItemChanged();
                    }
                } catch (NumberFormatException e) {
                    item.setPrice(0.0);
                    holder.tvItemSubtotal.setText("$0.00");
                    if (changeListener != null) {
                        changeListener.onItemChanged();
                    }
                }
            }
        });
        
        holder.etItemQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int quantity = Integer.parseInt(s.toString());
                    if (quantity > 0) {
                        item.setQuantity(quantity);
                        holder.tvItemSubtotal.setText("$" + currencyFormat.format(item.getSubtotal()));
                        if (changeListener != null) {
                            changeListener.onItemChanged();
                        }
                    }
                } catch (NumberFormatException e) {
                    item.setQuantity(1);
                    holder.tvItemSubtotal.setText("$" + currencyFormat.format(item.getSubtotal()));
                    if (changeListener != null) {
                        changeListener.onItemChanged();
                    }
                }
            }
        });
        
        holder.btnDeleteItem.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onItemDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        EditText etItemName, etItemPrice, etItemQuantity;
        TextView tvItemSubtotal;
        ImageView btnDeleteItem;
        RecyclerView rvPeopleSelection;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            etItemName = itemView.findViewById(R.id.etItemName);
            etItemPrice = itemView.findViewById(R.id.etItemPrice);
            etItemQuantity = itemView.findViewById(R.id.etItemQuantity);
            tvItemSubtotal = itemView.findViewById(R.id.tvItemSubtotal);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
            rvPeopleSelection = itemView.findViewById(R.id.rvPeopleSelection);
        }
    }
}
