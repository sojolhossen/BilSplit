package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axnovaxx.bilsplit.ItemizedSplitActivity.Person;

import java.util.List;

public class PeopleSelectionAdapter extends RecyclerView.Adapter<PeopleSelectionAdapter.SelectionViewHolder> {

    private List<Person> peopleList;
    private List<String> selectedPeople;
    private ItemAdapter.OnItemChangeListener changeListener;

    public PeopleSelectionAdapter(List<Person> peopleList, List<String> selectedPeople, 
                                ItemAdapter.OnItemChangeListener changeListener) {
        this.peopleList = peopleList;
        this.selectedPeople = selectedPeople;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person_selection, parent, false);
        return new SelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {
        Person person = peopleList.get(position);
        holder.tvPersonName.setText(person.getName());
        
        // Check if this person is selected
        boolean isSelected = selectedPeople.contains(person.getName());
        holder.cbSelected.setChecked(isSelected);
        
        holder.itemView.setOnClickListener(v -> {
            toggleSelection(person.getName(), holder.cbSelected);
        });
        
        holder.cbSelected.setOnClickListener(v -> {
            toggleSelection(person.getName(), holder.cbSelected);
        });
    }

    private void toggleSelection(String personName, CheckBox checkBox) {
        if (selectedPeople.contains(personName)) {
            selectedPeople.remove(personName);
            checkBox.setChecked(false);
        } else {
            selectedPeople.add(personName);
            checkBox.setChecked(true);
        }
        
        if (changeListener != null) {
            changeListener.onItemChanged();
        }
    }

    @Override
    public int getItemCount() {
        return peopleList.size();
    }

    static class SelectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPersonName;
        CheckBox cbSelected;

        SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            cbSelected = itemView.findViewById(R.id.cbSelected);
        }
    }
}
