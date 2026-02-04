package com.axnovaxx.bilsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class MemberPaymentAdapter extends RecyclerView.Adapter<MemberPaymentAdapter.MemberPaymentViewHolder> {

    private List<MemberPayment> memberPayments;
    private DecimalFormat currencyFormat;

    public static class MemberPayment {
        private String memberName;
        private double amount;

        public MemberPayment(String memberName, double amount) {
            this.memberName = memberName;
            this.amount = amount;
        }

        public String getMemberName() { return memberName; }
        public double getAmount() { return amount; }
    }

    public MemberPaymentAdapter(List<MemberPayment> memberPayments) {
        this.memberPayments = memberPayments;
        this.currencyFormat = new DecimalFormat("#0.00");
    }

    @NonNull
    @Override
    public MemberPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_payment, parent, false);
        return new MemberPaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberPaymentViewHolder holder, int position) {
        MemberPayment payment = memberPayments.get(position);
        
        holder.tvMemberName.setText(payment.getMemberName());
        holder.tvMemberAmount.setText("$" + currencyFormat.format(payment.getAmount()));
    }

    @Override
    public int getItemCount() {
        return memberPayments.size();
    }

    static class MemberPaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberAmount;

        MemberPaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberAmount = itemView.findViewById(R.id.tvMemberAmount);
        }
    }
}
