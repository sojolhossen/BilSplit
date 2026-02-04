package com.axnovaxx.bilsplit;

import java.util.List;

public class SavedGroup {
    private long id;
    private String name;
    private String createdAt;
    private String updatedAt;
    private List<String> members;

    public SavedGroup() {}

    public SavedGroup(String name, List<String> members) {
        this.name = name;
        this.members = members;
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    @Override
    public String toString() {
        return name + " (" + getMemberCount() + " members)";
    }
}
