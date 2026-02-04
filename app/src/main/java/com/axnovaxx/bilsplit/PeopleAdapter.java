package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axnovaxx.bilsplit.ItemizedSplitActivity.Person;

import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PersonViewHolder> {

    private List<Person> peopleList;
    private OnPersonDeleteListener deleteListener;

    public interface OnPersonDeleteListener {
        void onPersonDelete(int position);
    }

    public PeopleAdapter(List<Person> peopleList, OnPersonDeleteListener deleteListener) {
        this.peopleList = peopleList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        Person person = peopleList.get(position);
        holder.tvPersonName.setText(person.getName());
        
        holder.btnDeletePerson.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onPersonDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return peopleList.size();
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        TextView tvPersonName;
        ImageButton btnDeletePerson;

        PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            btnDeletePerson = itemView.findViewById(R.id.btnDeletePerson);
        }
    }
}
