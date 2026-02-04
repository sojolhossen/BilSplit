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

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<String> membersList;
    private OnMemberRemoveListener removeListener;

    public interface OnMemberRemoveListener {
        void onMemberRemove(String member);
    }

    public MemberAdapter(List<String> membersList, OnMemberRemoveListener removeListener) {
        this.membersList = membersList != null ? membersList : new ArrayList<>();
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String member = membersList.get(position);
        
        holder.tvMemberName.setText(member);
        
        holder.btnRemoveMember.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onMemberRemove(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    public void updateData(List<String> newMembersList) {
        this.membersList = newMembersList != null ? newMembersList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMember(String member) {
        if (member != null && !member.trim().isEmpty() && !membersList.contains(member.trim())) {
            membersList.add(member.trim());
            notifyItemInserted(membersList.size() - 1);
        }
    }

    public void removeMember(String member) {
        int position = membersList.indexOf(member);
        if (position != -1) {
            membersList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<String> getMembersList() {
        return new ArrayList<>(membersList);
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        ImageView btnRemoveMember;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            btnRemoveMember = itemView.findViewById(R.id.btnRemoveMember);
        }
    }
}
