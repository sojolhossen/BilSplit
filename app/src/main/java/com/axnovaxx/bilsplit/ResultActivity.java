package com.axnovaxx.bilsplit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
// ✅ ১. এই দুটি নতুন ইমপোর্ট দরকার
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    // ✅ ২. এখানে tvDate যোগ করা হয়েছে
    TextView tvOriginalBill, tvTipAmount, tvTotalBill, tvPeople, tvPerPerson, tvDate;
    Button btnShare, btnBackResult;
    LinearLayout receiptContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float density = v.getResources().getDisplayMetrics().density;
            int extraGap = (int) (24 * density);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + extraGap);
            return insets;
        });

        // Initialize Views
        tvOriginalBill = findViewById(R.id.tvOriginalBill);
        tvTipAmount = findViewById(R.id.tvTipAmount);
        tvTotalBill = findViewById(R.id.tvTotalBill);
        tvPeople = findViewById(R.id.tvPeople);
        tvPerPerson = findViewById(R.id.tvPerPerson);

        // ✅ ৩. নতুন ভিউ খুঁজে বের করা
        tvDate = findViewById(R.id.tvDate);

        btnShare = findViewById(R.id.btnShare);
        btnBackResult = findViewById(R.id.btnBackResult);

        receiptContainer = findViewById(R.id.receiptContainer);

        // Get Data
        Intent intent = getIntent();
        double billWithTax = intent.getDoubleExtra("BILL", 0.0);
        double tipPercent = intent.getDoubleExtra("TIP_PERCENT", 0.0);
        int people = intent.getIntExtra("PEOPLE", 1);

        String currencySymbol = intent.getStringExtra("CURRENCY");
        if (currencySymbol == null) currencySymbol = "$";

        // Calculate
        double tipAmount = (billWithTax * tipPercent) / 100;
        double grandTotal = billWithTax + tipAmount;
        double perPersonSplit = grandTotal / people;

        // Set Text
        tvOriginalBill.setText(String.format(Locale.US, "%s%.2f", currencySymbol, billWithTax));
        tvTipAmount.setText(String.format(Locale.US, "%s%.2f", currencySymbol, tipAmount));
        tvTotalBill.setText(String.format(Locale.US, "%s%.2f", currencySymbol, grandTotal));
        tvPeople.setText(String.format(Locale.US, "%d People", people));
        tvPerPerson.setText(String.format(Locale.US, "%s%.2f", currencySymbol, perPersonSplit));

        // ✅ ৪. বর্তমান তারিখ ও সময় সেট করার লজিক
        // ফরম্যাট: 01 Jan 2026, 10:00 AM
        String currentDateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        tvDate.setText("Date: " + currentDateTime);

        // Share Button Logic
        btnShare.setOnClickListener(v -> shareReceipt());

        btnBackResult.setOnClickListener(v -> finish());
    }

    private void shareReceipt() {
        Bitmap bitmap = getBitmapFromView(receiptContainer);

        try {
            File cachePath = new File(getCacheDir(), "images");

            if (!cachePath.exists()) {
                boolean created = cachePath.mkdirs();
                if (!created) {
                    Log.e("ResultActivity", "Failed to create directory");
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

        } catch (IOException e) {
            Log.e("ResultActivity", "Error sharing receipt", e);
            Toast.makeText(this, "Error sharing receipt", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        android.graphics.drawable.Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(android.graphics.Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }
}