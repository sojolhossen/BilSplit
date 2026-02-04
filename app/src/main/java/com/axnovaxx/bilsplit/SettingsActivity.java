package com.axnovaxx.bilsplit;

import android.content.Intent;
import android.content.SharedPreferences; // মেমোরি সেভ করার জন্য
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog; // পপ-আপ ডায়ালগের জন্য
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        applyThemeFromPreferences();
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ভিউগুলো খুঁজে বের করা
        ImageView btnBack = findViewById(R.id.btnBack);

        // ১. নতুন কারেন্সি মেনু যোগ করা হলো
        TextView menuTheme = findViewById(R.id.menuTheme);
        TextView menuCurrency = findViewById(R.id.menuCurrency);

        TextView menuPrivacy = findViewById(R.id.menuPrivacy);
        TextView menuRate = findViewById(R.id.menuRateUs);
        TextView menuShare = findViewById(R.id.menuShareApp);
        TextView menuContact = findViewById(R.id.menuContact);

        // --- বাটন লজিক ---

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Theme change logic
        menuTheme.setOnClickListener(v -> showThemeDialog());

        // ২. কারেন্সি চেঞ্জ লজিক (নতুন)
        menuCurrency.setOnClickListener(v -> showCurrencyDialog());

        // Share App Logic
        menuShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBody = "Check out this amazing Bill Splitter app! Download now: https://play.google.com/store/apps/details?id=" + getPackageName();
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Split Bills Easily");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Rate Us Logic
        menuRate.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        // Contact Us Logic
        menuContact.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:mostofadevcenter@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Bill Splitter");
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
        });

        // Privacy Policy Logic
        menuPrivacy.setOnClickListener(v -> {
            String url = "https://sites.google.com/view/billsplitter-privacy/home";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

    // ৩. কারেন্সি ডায়ালগ দেখানোর মেথড (নতুন)
    private void showCurrencyDialog() {
        // অপশনগুলোর লিস্ট
        String[] currencies = {"Dollar ($)", "Taka (৳)", "Rupee (₹)", "Euro (€)"};

        // আগে কী সেভ করা ছিল তা বের করা
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentSymbol = prefs.getString("currency_symbol", "$");

        // ডিফল্ট সিলেকশন ঠিক করা
        int checkedItem = switch (currentSymbol) {
            case "৳" -> 1;
            case "₹" -> 2;
            case "€" -> 3;
            default -> 0;
        };

        // ডায়ালগ তৈরি
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Currency");

        builder.setSingleChoiceItems(currencies, checkedItem, (dialog, which) -> {
            // ইউজার যেটা সিলেক্ট করবে
            String selectedSymbol = "$";
            if (which == 1) selectedSymbol = "৳";
            else if (which == 2) selectedSymbol = "₹";
            else if (which == 3) selectedSymbol = "€";

            // মেমোরিতে সেভ করা (Save to Storage)
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("currency_symbol", selectedSymbol);
            editor.apply();

            Toast.makeText(this, "Currency Updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // ডায়ালগ বন্ধ হবে
        });

        builder.show(); // ডায়ালগ দেখানো
    }

    // Theme dialog method
    private void showThemeDialog() {
        // Theme options
        String[] themes = {"Light Mode", "Dark Mode", "System Default"};

        // Get current theme preference
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentTheme = prefs.getString("theme_mode", "light");

        // Default selection
        int checkedItem = switch (currentTheme) {
            case "dark" -> 1;
            case "system" -> 2;
            default -> 0;
        };

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Theme");

        builder.setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
            // User selection
            String selectedTheme = "light";
            if (which == 1) selectedTheme = "dark";
            else if (which == 2) selectedTheme = "system";

            // Save to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("theme_mode", selectedTheme);
            editor.apply();

            // Apply theme change
            applyTheme(selectedTheme);

            Toast.makeText(this, "Theme Updated! Restart app to see full changes", Toast.LENGTH_SHORT).show();
            
            // Recreate activity to apply theme immediately
            recreate();
            dialog.dismiss();
        });

        builder.show(); // Show dialog
    }

    // Apply theme method
    private void applyTheme(String theme) {
        switch (theme) {
            case "dark":
                // Apply dark theme
                setTheme(androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar);
                break;
            case "light":
                // Apply light theme
                setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
                break;
            case "system":
                // Follow system theme
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    setTheme(androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar);
                } else {
                    setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
                }
                break;
        }
    }

    private void applyThemeFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String themeMode = prefs.getString("theme_mode", "light");
        
        switch (themeMode) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }
}