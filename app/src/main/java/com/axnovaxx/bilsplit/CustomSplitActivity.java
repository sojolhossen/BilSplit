package com.axnovaxx.bilsplit;

import android.content.SharedPreferences; // ১. মেমোরি রিড করার জন্য
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class CustomSplitActivity extends AppCompatActivity {

    ImageView btnBackCustom;
    EditText etTotalBillCustom;

    EditText etPerson1Name, etPerson1Amount;
    EditText etPerson2Name, etPerson2Amount;
    EditText etPerson3Name, etPerson3Amount;
    EditText etPerson4Name, etPerson4Amount;

    TextView tvStatus;
    Button btnCalculateCustom;

    // ২. কারেন্সি ভেরিয়েবল (ডিফল্ট $)
    String currencySymbol = "$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_custom_split);

        // ৩. সেটিংস থেকে কারেন্সি লোড করা
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currencySymbol = prefs.getString("currency_symbol", "$");

        // প্যাডিং ফিক্স (আপনার আগের কোড ঠিক আছে)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float density = v.getResources().getDisplayMetrics().density;
            int paddingSide = (int) (24 * density);
            int paddingBottom = (int) (24 * density);

            v.setPadding(
                    systemBars.left + paddingSide,
                    systemBars.top,
                    systemBars.right + paddingSide,
                    systemBars.bottom + paddingBottom
            );
            return insets;
        });

        // ভিউ খুঁজে বের করা
        btnBackCustom = findViewById(R.id.btnBackCustom);
        etTotalBillCustom = findViewById(R.id.etTotalBillCustom);

        etPerson1Name = findViewById(R.id.etPerson1Name);
        etPerson1Amount = findViewById(R.id.etPerson1Amount);

        etPerson2Name = findViewById(R.id.etPerson2Name);
        etPerson2Amount = findViewById(R.id.etPerson2Amount);

        etPerson3Name = findViewById(R.id.etPerson3Name);
        etPerson3Amount = findViewById(R.id.etPerson3Amount);

        etPerson4Name = findViewById(R.id.etPerson4Name);
        etPerson4Amount = findViewById(R.id.etPerson4Amount);

        tvStatus = findViewById(R.id.tvStatus);
        btnCalculateCustom = findViewById(R.id.btnCalculateCustom);

        // ৪. ইনপুট বক্সে Hint আপডেট করা (যাতে $0.00 এর বদলে ৳0.00 দেখায়)
        updateHints();

        // ক্লিক লিসেনার
        btnBackCustom.setOnClickListener(v -> finish());
        btnCalculateCustom.setOnClickListener(v -> calculateCustomSplit());
    }

    // নতুন মেথড: Hint আপডেট করার জন্য
    private void updateHints() {
        String hintText = currencySymbol + "0.00";
        etPerson1Amount.setHint(hintText);
        etPerson2Amount.setHint(hintText);
        etPerson3Amount.setHint(hintText);
        etPerson4Amount.setHint(hintText);
    }

    private void calculateCustomSplit() {
        String totalStr = etTotalBillCustom.getText().toString();

        if (totalStr.isEmpty()) {
            etTotalBillCustom.setError("Enter total bill");
            return;
        }

        try {
            double totalBill = Double.parseDouble(totalStr);

            double p1 = getAmount(etPerson1Amount);
            double p2 = getAmount(etPerson2Amount);
            double p3 = getAmount(etPerson3Amount);
            double p4 = getAmount(etPerson4Amount);

            double totalEntered = p1 + p2 + p3 + p4;
            double remaining = totalBill - totalEntered;

            // ৫. ক্যালকুলেশন রেজাল্টে কারেন্সি সিম্বল বসানো
            if (Math.abs(remaining) < 0.01) {
                // স্ট্রিং রিসোর্স থাকলে সেটা ব্যবহার করুন, নাহলে টেক্সট লিখুন
                tvStatus.setText(R.string.status_perfect);
                tvStatus.setTextColor(Color.parseColor("#5CC252"));
            } else if (remaining > 0) {
                // $ এর বদলে %s (currencySymbol) ব্যবহার করা হলো
                tvStatus.setText(String.format(Locale.US, "Remaining: %s%.2f", currencySymbol, remaining));
                tvStatus.setTextColor(Color.RED);
            } else {
                tvStatus.setText(String.format(Locale.US, "Overpaid by: %s%.2f", currencySymbol, Math.abs(remaining)));
                tvStatus.setTextColor(Color.parseColor("#FF5722"));
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private double getAmount(EditText et) {
        String str = et.getText().toString();
        if (str.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}