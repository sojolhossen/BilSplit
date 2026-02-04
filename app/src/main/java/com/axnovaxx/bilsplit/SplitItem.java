package com.axnovaxx.bilsplit;

public class SplitItem {
    private long id;
    private String name;
    private double price;
    private int quantity;
    private String assignedTo; // JSON array of person names

    public SplitItem() {}

    public SplitItem(String name, double price, int quantity, String assignedTo) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.assignedTo = assignedTo;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return name + " x" + quantity + " (" + assignedTo + ")";
    }
}
