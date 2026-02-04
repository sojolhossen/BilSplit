package com.axnovaxx.bilsplit;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axnovaxx.bilsplit.MemberPaymentAdapter.MemberPayment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryGroupsActivity extends AppCompatActivity {

    private ImageView btnBack, btnClearSearch;
    private EditText etSearch;
    private AppCompatButton btnTabHistory, btnTabGroups, btnAddGroup;
    private RecyclerView rvHistory, rvGroups, rvFilterChips;
    private LinearLayout layoutHistoryContent, layoutGroupsContent, layoutEmptyHistory, layoutEmptyGroups;

    private HistoryAdapter historyAdapter;
    private GroupAdapter groupAdapter;
    private FilterChipAdapter filterChipAdapter;
    
    private DatabaseHelper databaseHelper;
    private List<SplitHistory> allHistoryList;
    private List<SplitHistory> filteredHistoryList;
    private List<SavedGroup> groupsList;
    
    private boolean isHistoryTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_groups);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Initialize lists
        allHistoryList = new ArrayList<>();
        filteredHistoryList = new ArrayList<>();
        groupsList = new ArrayList<>();
        
        // Initialize views
        initViews();
        
        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup search functionality
        setupSearch();
        
        // Load data
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        etSearch = findViewById(R.id.etSearch);
        btnTabHistory = findViewById(R.id.btnTabHistory);
        btnTabGroups = findViewById(R.id.btnTabGroups);
        btnAddGroup = findViewById(R.id.btnAddGroup);
        rvHistory = findViewById(R.id.rvHistory);
        rvGroups = findViewById(R.id.rvGroups);
        layoutHistoryContent = findViewById(R.id.layoutHistoryContent);
        layoutGroupsContent = findViewById(R.id.layoutGroupsContent);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory);
        layoutEmptyGroups = findViewById(R.id.layoutEmptyGroups);
    }

    private void setupRecyclerViews() {
        // Setup History RecyclerView
        historyAdapter = new HistoryAdapter(filteredHistoryList, this::onHistoryItemClick, this::onHistoryDelete);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
        
        // Setup Groups RecyclerView
        groupAdapter = new GroupAdapter(groupsList, this::onGroupClick, this::onGroupDelete, this::onGroupEdit);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(groupAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnTabHistory.setOnClickListener(v -> switchToHistoryTab());
        
        btnTabGroups.setOnClickListener(v -> switchToGroupsTab());
        
        btnAddGroup.setOnClickListener(v -> showAddGroupDialog());
        
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                
                if (isHistoryTab) {
                    filterHistory(query);
                } else {
                    filterGroups(query);
                }
            }
        });
    }

    private void loadData() {
        // Load history data
        allHistoryList = databaseHelper.getAllSplitHistory();
        filteredHistoryList.clear();
        filteredHistoryList.addAll(allHistoryList);
        historyAdapter.notifyDataSetChanged();
        
        // Load groups data
        groupsList = databaseHelper.getAllSavedGroups();
        
        // Debug logging for groups
        Log.d("HistoryGroups", "Initial groups loaded: " + groupsList.size());
        for (SavedGroup group : groupsList) {
            Log.d("HistoryGroups", "Initial Group: " + group.getName() + ", Members: " + group.getMemberCount());
        }
        
        groupAdapter.notifyDataSetChanged();
        
        // Update empty states
        updateEmptyStates();
    }

    private void switchToHistoryTab() {
        isHistoryTab = true;
        
        btnTabHistory.setTextColor(getColor(R.color.green_primary));
        btnTabHistory.setBackgroundResource(R.drawable.btn_tab_selected);
        btnTabGroups.setTextColor(getColor(R.color.text_secondary));
        btnTabGroups.setBackgroundResource(R.drawable.btn_tab_unselected);
        
        layoutHistoryContent.setVisibility(View.VISIBLE);
        layoutGroupsContent.setVisibility(View.GONE);
        
        updateEmptyStates();
    }

    private void switchToGroupsTab() {
        isHistoryTab = false;
        
        btnTabGroups.setTextColor(getColor(R.color.green_primary));
        btnTabGroups.setBackgroundResource(R.drawable.btn_tab_selected);
        btnTabHistory.setTextColor(getColor(R.color.text_secondary));
        btnTabHistory.setBackgroundResource(R.drawable.btn_tab_unselected);
        
        layoutGroupsContent.setVisibility(View.VISIBLE);
        layoutHistoryContent.setVisibility(View.GONE);
        
        // Refresh groups data when switching to groups tab
        refreshGroupsData();
        
        updateEmptyStates();
    }
    
    private void refreshGroupsData() {
        try {
            groupsList = databaseHelper.getAllSavedGroups();
            
            // Debug logging
            Log.d("HistoryGroups", "Groups loaded: " + groupsList.size());
            for (SavedGroup group : groupsList) {
                Log.d("HistoryGroups", "Group: " + group.getName() + ", Members: " + group.getMemberCount());
            }
            
            if (groupAdapter != null) {
                // Use updateData method instead of notifyDataSetChanged
                groupAdapter.updateData(groupsList);
                Log.d("HistoryGroups", "Adapter updated with new data");
            } else {
                Log.e("HistoryGroups", "GroupAdapter is null!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("HistoryGroups", "Error loading groups", e);
        }
    }

    private void filterHistory(String query) {
        filteredHistoryList.clear();
        
        if (query.isEmpty()) {
            filteredHistoryList.addAll(allHistoryList);
        } else {
            String lowercaseQuery = query.toLowerCase();
            for (SplitHistory history : allHistoryList) {
                // Search by notes, split type, or date
                if ((history.getNotes() != null && history.getNotes().toLowerCase().contains(lowercaseQuery)) ||
                    history.getSplitType().toLowerCase().contains(lowercaseQuery) ||
                    history.getCreatedAt().toLowerCase().contains(lowercaseQuery)) {
                    filteredHistoryList.add(history);
                }
            }
        }
        
        historyAdapter.notifyDataSetChanged();
        updateEmptyStates();
    }

    private void filterGroups(String query) {
        // TODO: Implement group filtering
        groupAdapter.notifyDataSetChanged();
        updateEmptyStates();
    }

    private void onFilterSelected(String filter) {
        // Filter functionality removed
        Toast.makeText(this, "Filter: " + filter, Toast.LENGTH_SHORT).show();
    }

    private void onHistoryItemClick(SplitHistory history) {
        // Show expense details dialog
        showExpenseDetailsDialog(history);
    }

    private void onHistoryDelete(SplitHistory history) {
        databaseHelper.deleteSplitHistory(history.getId());
        allHistoryList.remove(history);
        filteredHistoryList.remove(history);
        historyAdapter.notifyDataSetChanged();
        updateEmptyStates();
        Toast.makeText(this, "History deleted", Toast.LENGTH_SHORT).show();
    }

    private void onGroupClick(SavedGroup group) {
        // TODO: Implement group click functionality
        Toast.makeText(this, "Group clicked: " + group.getName(), Toast.LENGTH_SHORT).show();
    }

    private void onGroupDelete(SavedGroup group) {
        databaseHelper.deleteSavedGroup(group.getId());
        groupsList.remove(group);
        groupAdapter.notifyDataSetChanged();
        updateEmptyStates();
        Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
    }

    private void onGroupEdit(SavedGroup group) {
        showEditGroupDialog(group);
    }

    private void showEditGroupDialog(SavedGroup group) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_group, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Initialize dialog views
        EditText etGroupName = dialogView.findViewById(R.id.etGroupName);
        EditText etNewMember = dialogView.findViewById(R.id.etNewMember);
        TextView btnAddMember = dialogView.findViewById(R.id.btnAddMember);
        TextView btnSave = dialogView.findViewById(R.id.btnSave);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        RecyclerView rvMembers = dialogView.findViewById(R.id.rvMembers);
        
        // Set current group name
        etGroupName.setText(group.getName());
        
        // Setup members list
        List<String> members = new ArrayList<>(group.getMembers() != null ? group.getMembers() : new ArrayList<>());
        MemberAdapter memberAdapter = new MemberAdapter(members, this::onMemberRemove);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);
        
        // Add member button
        btnAddMember.setOnClickListener(v -> {
            String newMember = etNewMember.getText().toString().trim();
            if (!newMember.isEmpty()) {
                memberAdapter.addMember(newMember);
                etNewMember.setText("");
            }
        });
        
        // Save button
        btnSave.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            if (groupName.isEmpty()) {
                etGroupName.setError("Please enter group name");
                return;
            }
            
            // Update group
            group.setName(groupName);
            group.setMembers(memberAdapter.getMembersList());
            
            // Save to database
            try {
                databaseHelper.updateGroupMembers(group.getId(), memberAdapter.getMembersList());
                refreshGroupsData();
                Toast.makeText(this, "Group updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error updating group", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    
    private void onMemberRemove(String member) {
        // Member will be removed by adapter
        Log.d("HistoryGroups", "Member removed: " + member);
    }

    private void showAddGroupDialog() {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_group, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Initialize dialog views
        EditText etGroupName = dialogView.findViewById(R.id.etGroupName);
        EditText etGroupDescription = dialogView.findViewById(R.id.etGroupDescription);
        TextView btnSaveGroup = dialogView.findViewById(R.id.btnSaveGroup);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        // Save group button click
        btnSaveGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            
            if (groupName.isEmpty()) {
                etGroupName.setError("Please enter group name");
                return;
            }
            
            // Create new group
            SavedGroup newGroup = new SavedGroup();
            newGroup.setName(groupName);
            newGroup.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new java.util.Date()));
            
            // Save to database
            try {
                long result = databaseHelper.insertSavedGroup(newGroup);
                if (result != -1) {
                    // Set the ID from database
                    newGroup.setId(result);
                    
                    // Add to list and refresh
                    groupsList.add(newGroup);
                    groupAdapter.notifyDataSetChanged();
                    updateEmptyStates();
                    
                    Toast.makeText(this, "Group added successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Error adding group", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error adding group", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void updateEmptyStates() {
        if (isHistoryTab) {
            boolean isEmpty = filteredHistoryList.isEmpty();
            layoutEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        } else {
            boolean isEmpty = groupsList.isEmpty();
            layoutEmptyGroups.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvGroups.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void showExpenseDetailsDialog(SplitHistory history) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense_details, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Initialize dialog views
        TextView tvDialogDate = dialogView.findViewById(R.id.tvDialogDate);
        TextView tvDialogSplitType = dialogView.findViewById(R.id.tvDialogSplitType);
        TextView tvDialogPeopleCount = dialogView.findViewById(R.id.tvDialogPeopleCount);
        TextView tvDialogBillAmount = dialogView.findViewById(R.id.tvDialogBillAmount);
        TextView tvDialogTotalAmount = dialogView.findViewById(R.id.tvDialogTotalAmount);
        TextView tvDialogNotes = dialogView.findViewById(R.id.tvDialogNotes);
        LinearLayout layoutItemsSection = dialogView.findViewById(R.id.layoutItemsSection);
        RecyclerView rvDialogItems = dialogView.findViewById(R.id.rvDialogItems);
        RecyclerView rvMemberPayments = dialogView.findViewById(R.id.rvMemberPayments);
        AppCompatButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        
        // Set data
        tvDialogDate.setText(history.getCreatedAt());
        tvDialogSplitType.setText(history.getSplitType());
        tvDialogPeopleCount.setText(history.getPeopleCount() + " People");
        
        // Calculate amounts (simplified - just use total amount)
        double totalAmount = history.getFinalAmount();
        
        tvDialogBillAmount.setText(String.format("$%.2f", totalAmount));
        tvDialogTotalAmount.setText(String.format("$%.2f", totalAmount));
        
        // Create member payments (simplified - equal split)
        List<MemberPayment> memberPayments = new ArrayList<>();
        double perPerson = history.getPeopleCount() > 0 ? totalAmount / history.getPeopleCount() : 0;
        
        for (int i = 1; i <= history.getPeopleCount(); i++) {
            memberPayments.add(new MemberPayment("Person " + i, perPerson));
        }
        
        // Setup member payments RecyclerView
        MemberPaymentAdapter memberAdapter = new MemberPaymentAdapter(memberPayments);
        rvMemberPayments.setLayoutManager(new LinearLayoutManager(this));
        rvMemberPayments.setAdapter(memberAdapter);
        
        // Show notes if available
        if (history.getNotes() != null && !history.getNotes().isEmpty()) {
            tvDialogNotes.setText("Notes: " + history.getNotes());
            tvDialogNotes.setVisibility(View.VISIBLE);
        }
        
        // Show items section if it's an itemized split
        if (history.getItems() != null && !history.getItems().isEmpty() && 
            "Itemized Split".equals(history.getSplitType())) {
            layoutItemsSection.setVisibility(View.VISIBLE);
            
            // Setup items RecyclerView
            DialogItemAdapter itemAdapter = new DialogItemAdapter(history.getItems());
            rvDialogItems.setLayoutManager(new LinearLayoutManager(this));
            rvDialogItems.setAdapter(itemAdapter);
        }
        
        // Close button
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
    }
}
