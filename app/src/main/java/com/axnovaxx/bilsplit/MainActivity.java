package com.axnovaxx.bilsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

// ✅ এই লাইনটি যোগ করতে আপনি ভুলে গিয়েছিলেন
import androidx.core.splashscreen.SplashScreen;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText etBillAmount, etTip, etPeople, etTipAmountInput, etTax;
    Button btnSplitNow, btnCustomSplit;
    ImageView btnSettings;

    String currencySymbol = "$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ এই লাইনটি আপনি মিস করেছিলেন! এটি অবশ্যই super.onCreate এর আগে থাকতে হবে
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
        btnSettings = findViewById(R.id.btnSettings);

        // Click Listeners
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnSplitNow.setOnClickListener(v -> processAndNavigate());

        btnCustomSplit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CustomSplitActivity.class);
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
}