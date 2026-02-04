package com.axnovaxx.bilsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

// 
import androidx.core.splashscreen.SplashScreen;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText etBillAmount, etTip, etPeople, etTipAmountInput, etTax;
    Button btnSplitNow, btnCustomSplit, btnItemizedSplit;
    ImageView btnSettings, btnHistory, btnExpenseTracker;

    String currencySymbol = "$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        applyThemeFromPreferences();
        
        // 
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float density = v.getResources().getDisplayMetrics().density;
            int padding = (int) (24 * density);

            v.setPadding(
                    systemBars.left + padding,
                    systemBars.top + padding,
                    systemBars.right + padding,
                    systemBars.bottom + padding
            );
            return insets;
        });

        // Initialization
        etBillAmount = findViewById(R.id.etBillAmount);
        etTax = findViewById(R.id.etTax);
        etTip = findViewById(R.id.etTip);
        etTipAmountInput = findViewById(R.id.etTipAmountInput);
        etPeople = findViewById(R.id.etPeople);
        btnSplitNow = findViewById(R.id.btnSplitNow);
        btnCustomSplit = findViewById(R.id.btnCustomSplit);
        btnItemizedSplit = findViewById(R.id.btnItemizedSplit);
        btnSettings = findViewById(R.id.btnSettings);
        btnHistory = findViewById(R.id.btnHistory);
        btnExpenseTracker = findViewById(R.id.btnExpenseTracker);

        // Click Listeners
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryGroupsActivity.class);
            startActivity(intent);
        });

        btnExpenseTracker.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExpenseTrackerActivity.class);
            startActivity(intent);
        });

        btnSplitNow.setOnClickListener(v -> processAndNavigate());

        btnCustomSplit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CustomSplitActivity.class);
            startActivity(intent);
        });

        btnItemizedSplit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ItemizedSplitActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currencySymbol = prefs.getString("currency_symbol", "$");
    }

    private void processAndNavigate() {
        String billStr = etBillAmount.getText().toString();
        String taxStr = etTax.getText().toString();
        String tipPercentStr = etTip.getText().toString();
        String tipAmountStr = etTipAmountInput.getText().toString();
        String peopleStr = etPeople.getText().toString();

        if (billStr.isEmpty()) {
            etBillAmount.setError("Please enter bill amount");
            return;
        }
        if (peopleStr.isEmpty() || peopleStr.equals("0")) {
            etPeople.setError("Enter valid number");
            return;
        }

        try {
            double bill = Double.parseDouble(billStr);
            int people = Integer.parseInt(peopleStr);

            double taxPercent = 0.0;
            if (!taxStr.isEmpty()) {
                taxPercent = Double.parseDouble(taxStr);
            }
            double taxAmount = (bill * taxPercent) / 100;
            double billWithTax = bill + taxAmount;

            double finalTipPercent = 0.0;
            if (!tipAmountStr.isEmpty()) {
                double tipAmount = Double.parseDouble(tipAmountStr);
                finalTipPercent = (tipAmount / billWithTax) * 100;
            } else if (!tipPercentStr.isEmpty()) {
                finalTipPercent = Double.parseDouble(tipPercentStr);
            }

            // Save to history before navigating
            saveToHistory(billWithTax, finalTipPercent, taxAmount, people);

            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("BILL", billWithTax);
            intent.putExtra("TIP_PERCENT", finalTipPercent);
            intent.putExtra("PEOPLE", people);
            intent.putExtra("CURRENCY", currencySymbol);

            startActivity(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToHistory(double totalAmount, double tipPercent, double taxAmount, int peopleCount) {
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
            
            // Create SplitHistory object
            SplitHistory splitHistory = new SplitHistory();
            splitHistory.setTotalAmount(totalAmount);
            splitHistory.setTipPercentage(tipPercent);
            splitHistory.setTaxAmount(taxAmount);
            splitHistory.setPeopleCount(peopleCount);
            splitHistory.setCurrencySymbol(currencySymbol);
            splitHistory.setSplitType("Equal Split");
            splitHistory.setNotes("Regular split with " + peopleCount + " people");
            
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

    private void applyThemeFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String themeMode = prefs.getString("theme_mode", "light");
        
        switch (themeMode) {
            case "dark":
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            default:
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }
}