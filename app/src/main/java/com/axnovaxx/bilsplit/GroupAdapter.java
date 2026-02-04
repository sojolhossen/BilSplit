package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<SavedGroup> groupsList;
    private OnGroupClickListener clickListener;
    private OnGroupDeleteListener deleteListener;
    private OnGroupEditListener editListener;

    public interface OnGroupClickListener {
        void onGroupClick(SavedGroup group);
    }

    public interface OnGroupDeleteListener {
        void onGroupDelete(SavedGroup group);
    }

    public interface OnGroupEditListener {
        void onGroupEdit(SavedGroup group);
    }

    public GroupAdapter(List<SavedGroup> groupsList, OnGroupClickListener clickListener, OnGroupDeleteListener deleteListener, OnGroupEditListener editListener) {
        this.groupsList = groupsList != null ? groupsList : new ArrayList<>();
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        SavedGroup group = groupsList.get(position);
        
        holder.tvGroupName.setText(group.getName());
        holder.tvMemberCount.setText(group.getMemberCount() + " members");
        
        // Show first few members as preview
        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            StringBuilder membersPreview = new StringBuilder();
            for (int i = 0; i < Math.min(3, group.getMembers().size()); i++) {
                if (i > 0) membersPreview.append(", ");
                membersPreview.append(group.getMembers().get(i));
            }
            if (group.getMembers().size() > 3) {
                membersPreview.append(" +").append(group.getMembers().size() - 3).append(" more");
            }
            holder.tvMembersPreview.setText(membersPreview.toString());
        } else {
            holder.tvMembersPreview.setText("No members");
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGroupClick(group);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onGroupDelete(group);
            }
        });
        
        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onGroupEdit(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public void updateData(List<SavedGroup> newGroupsList) {
        this.groupsList = newGroupsList != null ? newGroupsList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvMemberCount, tvMembersPreview;
        ImageView btnDelete, btnEdit;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvMembersPreview = itemView.findViewById(R.id.tvMembersPreview);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
