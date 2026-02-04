package com.axnovaxx.bilsplit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemizedResultActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalBill, tvPeopleCount;
    private RecyclerView rvSplitResults;
    private android.widget.Button btnShare, btnNewSplit;

    private SplitResultAdapter resultAdapter;
    private DecimalFormat currencyFormat;
    private List<SplitResultItem> splitResultItems;

    public static class SplitResultItem {
        private String personName;
        private double amount;
        private int itemCount;
        private double percentage;

        public SplitResultItem(String personName, double amount, int itemCount, double percentage) {
            this.personName = personName;
            this.amount = amount;
            this.itemCount = itemCount;
            this.percentage = percentage;
        }

        // Getters
        public String getPersonName() { return personName; }
        public double getAmount() { return amount; }
        public int getItemCount() { return itemCount; }
        public double getPercentage() { return percentage; }
        
        public String getInitial() {
            return personName.isEmpty() ? "?" : String.valueOf(personName.charAt(0)).toUpperCase();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemized_result);

        // Initialize currency format
        currencyFormat = new DecimalFormat("#0.00");
        
        // Initialize views
        initViews();
        
        // Get data from intent
        getDataFromIntent();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalBill = findViewById(R.id.tvTotalBill);
        tvPeopleCount = findViewById(R.id.tvPeopleCount);
        rvSplitResults = findViewById(R.id.rvSplitResults);
        btnShare = findViewById(R.id.btnShare);
        btnNewSplit = findViewById(R.id.btnNewSplit);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        
        // Get splits data
        ArrayList<Double> splits = (ArrayList<Double>) intent.getSerializableExtra("SPLITS");
        ArrayList<String> peopleNames = (ArrayList<String>) intent.getSerializableExtra("PEOPLE_NAMES");
        double totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0);
        String currencySymbol = intent.getStringExtra("CURRENCY");
        
        if (currencySymbol == null) currencySymbol = "$";
        
        // Set total bill
        tvTotalBill.setText(currencySymbol + currencyFormat.format(totalAmount));
        tvPeopleCount.setText("Split among " + peopleNames.size() + " people");
        
        // Create split result items
        splitResultItems = new ArrayList<>();
        for (int i = 0; i < peopleNames.size(); i++) {
            String personName = peopleNames.get(i);
            double amount = splits.get(i);
            double percentage = totalAmount > 0 ? (amount / totalAmount) * 100 : 0;
            
            // Count items for this person (simplified - in real app you'd track this)
            int itemCount = 1; // Default to 1 item per person
            
            splitResultItems.add(new SplitResultItem(personName, amount, itemCount, percentage));
        }
    }

    private void setupRecyclerView() {
        resultAdapter = new SplitResultAdapter(splitResultItems, currencyFormat);
        rvSplitResults.setLayoutManager(new LinearLayoutManager(this));
        rvSplitResults.setAdapter(resultAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnShare.setOnClickListener(v -> shareResult());
        
        btnNewSplit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ItemizedSplitActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void shareResult() {
        // Get receipt container for image sharing
        View receiptContainer = findViewById(R.id.receiptContainer);
        
        // Create image share like ResultActivity
        try {
            Bitmap bitmap = getBitmapFromView(receiptContainer);
            
            // Save to cache
            File cachePath = new File(getCacheDir(), "images");
            
            if (!cachePath.exists()) {
                boolean created = cachePath.mkdirs();
                if (!created) {
                    Toast.makeText(this, "Error saving receipt", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            FileOutputStream stream = new FileOutputStream(cachePath + "/receipt.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            
            File newFile = new File(cachePath, "receipt.png");
            Uri contentUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", newFile);
            
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, "Share Receipt Via"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sharing receipt", Toast.LENGTH_SHORT).show();
            
            // Fallback to text sharing
            shareResultAsText();
        }
    }
    
    private void shareResultAsText() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("💰 Itemized Bill Split Result\n");
        shareText.append("===================\n\n");
        
        // Add date
        String currentDateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        shareText.append("Date: ").append(currentDateTime).append("\n\n");
        
        // Add total bill
        shareText.append("💵 Total Bill: ").append(tvTotalBill.getText().toString()).append("\n");
        shareText.append("👥 Split Among: ").append(tvPeopleCount.getText().toString()).append("\n\n");
        
        // Add individual splits
        shareText.append("Individual Splits:\n");
        for (SplitResultItem item : splitResultItems) {
            shareText.append("👤 ").append(item.getPersonName())
                    .append(": $").append(String.format("%.2f", item.getAmount()))
                    .append(" (").append(String.format("%.1f", item.getPercentage())).append("%)\n");
        }
        
        shareText.append("\n Generated by Bill Split App");
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Split Result"));
    }
    
    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        }
        view.draw(canvas);
        return returnedBitmap;
    }
}
