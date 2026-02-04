package com.axnovaxx.bilsplit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemizedSplitActivity extends AppCompatActivity {

    private RecyclerView rvPeople, rvItems;
    private ImageView btnBack, btnAddItem, btnAddPerson, btnAddItem2;
    private TextView tvTotalAmount;
    private android.widget.Button btnCalculateSplit, btnSaveGroup;
    
    private PeopleAdapter peopleAdapter;
    private ItemAdapter itemAdapter;
    
    private List<Person> peopleList;
    private List<ItemizedItem> itemsList;
    private DecimalFormat currencyFormat;
    
    private String currencySymbol = "$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemized_split);

        // Initialize currency format
        currencyFormat = new DecimalFormat("#0.00");
        
        // Initialize lists
        peopleList = new ArrayList<>();
        itemsList = new ArrayList<>();
        
        // Initialize views
        initViews();
        
        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup text watchers
        setupTextWatchers();
        
        // No demo data - start with empty lists
    }

    private void initViews() {
        rvPeople = findViewById(R.id.rvPeople);
        rvItems = findViewById(R.id.rvItems);
        btnBack = findViewById(R.id.btnBack);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnAddPerson = findViewById(R.id.btnAddPerson);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnCalculateSplit = findViewById(R.id.btnCalculateSplit);
        btnSaveGroup = findViewById(R.id.btnSaveGroup);
    }

    private void setupRecyclerViews() {
        // Setup People RecyclerView
        peopleAdapter = new PeopleAdapter(peopleList, this::onPersonDelete);
        rvPeople.setLayoutManager(new LinearLayoutManager(this));
        rvPeople.setAdapter(peopleAdapter);
        
        // Setup Items RecyclerView
        itemAdapter = new ItemAdapter(itemsList, peopleList, this::onItemDelete, this::onItemChanged);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnAddItem.setOnClickListener(v -> addNewItem());
        
        btnAddPerson.setOnClickListener(v -> addNewPerson());
        
        // Find the second add item button (in items section)
        btnAddItem2 = findViewById(R.id.btnAddItem2);
        btnAddItem2.setOnClickListener(v -> addNewItem());
        
        btnCalculateSplit.setOnClickListener(v -> calculateSplit());
        
        btnSaveGroup.setOnClickListener(v -> saveGroup());
    }

    private void setupTextWatchers() {
        // Text watchers can be added here if needed for other fields
    }

    private void addNewPerson() {
        // Show dialog to add person
        showAddPersonDialog();
    }
    
    private void showAddPersonDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_person, null);
        builder.setView(dialogView);
        
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        
        EditText etPersonName = dialogView.findViewById(R.id.etPersonName);
        android.widget.Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        btnAdd.setOnClickListener(v -> {
            String name = etPersonName.getText().toString().trim();
            if (name.isEmpty()) {
                etPersonName.setError("Enter person name");
                return;
            }
            
            // Check for duplicate names
            for (Person person : peopleList) {
                if (person.getName().equalsIgnoreCase(name)) {
                    etPersonName.setError("Person already exists");
                    return;
                }
            }
            
            peopleList.add(new Person(name));
            peopleAdapter.notifyDataSetChanged();
            itemAdapter.notifyDataSetChanged();
            
            Toast.makeText(this, "Person added: " + name, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void addNewItem() {
        if (peopleList.isEmpty()) {
            Toast.makeText(this, "Add people first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        itemsList.add(new ItemizedItem("Item " + (itemsList.size() + 1), 0.0, 1));
        itemAdapter.notifyDataSetChanged();
        updateTotalAmount();
    }

    private void onPersonDelete(int position) {
        peopleList.remove(position);
        peopleAdapter.notifyDataSetChanged();
        itemAdapter.notifyDataSetChanged(); // Update items to remove deleted person from selections
    }

    private void onItemDelete(int position) {
        itemsList.remove(position);
        itemAdapter.notifyDataSetChanged();
        updateTotalAmount();
    }

    private void onItemChanged() {
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        double total = 0.0;
        for (ItemizedItem item : itemsList) {
            total += item.getSubtotal();
        }
        tvTotalAmount.setText(currencySymbol + currencyFormat.format(total));
    }

    private void calculateSplit() {
        if (peopleList.isEmpty()) {
            Toast.makeText(this, "Add people first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (itemsList.isEmpty()) {
            Toast.makeText(this, "Add items first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate items
        for (ItemizedItem item : itemsList) {
            if (item.getName().trim().isEmpty()) {
                Toast.makeText(this, "Enter item name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (item.getPrice() <= 0) {
                Toast.makeText(this, "Enter valid item price", Toast.LENGTH_SHORT).show();
                return;
            }
            if (item.getSelectedPeople().isEmpty()) {
                Toast.makeText(this, "Select people for: " + item.getName(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Calculate splits
        Map<String, Double> splits = new HashMap<>();
        for (Person person : peopleList) {
            splits.put(person.getName(), 0.0);
        }
        
        for (ItemizedItem item : itemsList) {
            double splitAmount = item.getSubtotal() / item.getSelectedPeople().size();
            for (String personName : item.getSelectedPeople()) {
                splits.put(personName, splits.get(personName) + splitAmount);
            }
        }
        
        // Calculate total and other required data for ResultActivity
        double totalAmount = 0.0;
        for (ItemizedItem item : itemsList) {
            totalAmount += item.getSubtotal();
        }
        
        // Navigate to new itemized result activity
        Intent intent = new Intent(this, ItemizedResultActivity.class);
        intent.putExtra("SPLITS", new ArrayList<>(splits.values()));
        intent.putExtra("PEOPLE_NAMES", new ArrayList<>(splits.keySet()));
        intent.putExtra("TOTAL_AMOUNT", totalAmount);
        intent.putExtra("CURRENCY", currencySymbol);
        intent.putExtra("IS_ITEMIZED", true);
        
        // Save to history before navigating
        saveToHistory(totalAmount, splits);
        
        startActivity(intent);
    }

    private void saveToHistory(double totalAmount, Map<String, Double> splits) {
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
            
            // Create SplitHistory object
            SplitHistory splitHistory = new SplitHistory();
            splitHistory.setTotalAmount(totalAmount);
            splitHistory.setTipPercentage(0.0);
            splitHistory.setTaxAmount(0.0);
            splitHistory.setPeopleCount(peopleList.size());
            splitHistory.setCurrencySymbol(currencySymbol);
            splitHistory.setSplitType("Itemized Split");
            splitHistory.setNotes("Itemized split with " + itemsList.size() + " items");
            
            // Create items list for history
            List<SplitItem> splitItems = new ArrayList<>();
            for (ItemizedItem item : itemsList) {
                SplitItem splitItem = new SplitItem();
                splitItem.setName(item.getName());
                splitItem.setPrice(item.getPrice());
                splitItem.setQuantity(item.getQuantity());
                
                // Convert List<String> to JSON string for assignedTo
                JSONArray jsonArray = new JSONArray(item.getSelectedPeople());
                splitItem.setAssignedTo(jsonArray.toString());
                
                splitItems.add(splitItem);
            }
            splitHistory.setItems(splitItems);
            
            // Save to database
            long result = databaseHelper.insertSplitHistory(splitHistory);
            if (result != -1) {
                Toast.makeText(this, "Saved to history", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving to history", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGroup() {
        if (peopleList.isEmpty()) {
            Toast.makeText(this, "Add people first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
            
            // Create new group
            SavedGroup newGroup = new SavedGroup();
            newGroup.setName("Group " + new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
            newGroup.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
            
            // Add people as members
            List<String> members = new ArrayList<>();
            for (Person person : peopleList) {
                members.add(person.getName());
            }
            newGroup.setMembers(members);
            
            // Save to database
            long result = databaseHelper.insertSavedGroup(newGroup);
            if (result != -1) {
                // Set the ID from database
                newGroup.setId(result);
                
                Toast.makeText(this, "Group saved successfully", Toast.LENGTH_SHORT).show();
                
                // Navigate to history groups to show the saved group
                Intent intent = new Intent(this, HistoryGroupsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error saving group", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving group", Toast.LENGTH_SHORT).show();
        }
    }

    // Person model class
    public static class Person {
        private String name;
        
        public Person(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }

    // ItemizedItem model class
    public static class ItemizedItem {
        private String name;
        private double price;
        private int quantity;
        private List<String> selectedPeople;
        
        public ItemizedItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.selectedPeople = new ArrayList<>();
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
        
        public List<String> getSelectedPeople() {
            return selectedPeople;
        }
        
        public double getSubtotal() {
            return price * quantity;
        }
    }
}
