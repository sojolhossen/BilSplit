package com.axnovaxx.bilsplit;

import java.util.List;

public class SplitHistory {
    private long id;
    private double totalAmount;
    private double tipPercentage;
    private double taxAmount;
    private int peopleCount;
    private String currencySymbol;
    private String splitType; // regular, custom, itemized
    private String notes;
    private String createdAt;
    private String updatedAt;
    private List<SplitItem> items;

    public SplitHistory() {}

    public SplitHistory(double totalAmount, double tipPercentage, double taxAmount, 
                       int peopleCount, String currencySymbol, String splitType) {
        this.totalAmount = totalAmount;
        this.tipPercentage = tipPercentage;
        this.taxAmount = taxAmount;
        this.peopleCount = peopleCount;
        this.currencySymbol = currencySymbol;
        this.splitType = splitType;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTipPercentage() {
        return tipPercentage;
    }

    public void setTipPercentage(double tipPercentage) {
        this.tipPercentage = tipPercentage;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getSplitType() {
        return splitType;
    }

    public void setSplitType(String splitType) {
        this.splitType = splitType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SplitItem> getItems() {
        return items;
    }

    public void setItems(List<SplitItem> items) {
        this.items = items;
    }

    public double getFinalAmount() {
        return totalAmount + taxAmount + (totalAmount * tipPercentage / 100);
    }

    public double getAmountPerPerson() {
        if (peopleCount > 0) {
            return getFinalAmount() / peopleCount;
        }
        return 0;
    }
}
