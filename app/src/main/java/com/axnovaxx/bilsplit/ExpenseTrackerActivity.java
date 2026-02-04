package com.axnovaxx.bilsplit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseTrackerActivity extends AppCompatActivity {

    private ImageView btnBack, btnExport, btnPreviousMonth, btnNextMonth;
    private TextView tvCurrentMonth, tvTotalSpent, tvSplitCount, tvChartPlaceholder;
    private RecyclerView rvExpenseHistory;
    private android.widget.Button btnViewAllHistory;

    private ExpenseHistoryAdapter historyAdapter;
    private DatabaseHelper databaseHelper;
    private DecimalFormat currencyFormat;
    private SimpleDateFormat monthFormat;
    
    private Calendar currentMonth;
    private List<SplitHistory> expenseHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_tracker);

        // Initialize helpers
        databaseHelper = DatabaseHelper.getInstance(this);
        currencyFormat = new DecimalFormat("#0.00");
        monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        
        // Initialize calendar
        currentMonth = Calendar.getInstance();
        
        // Initialize views
        initViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load data
        loadExpenseData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnExport = findViewById(R.id.btnExport);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvSplitCount = findViewById(R.id.tvSplitCount);
        tvChartPlaceholder = findViewById(R.id.tvChartPlaceholder);
        rvExpenseHistory = findViewById(R.id.rvExpenseHistory);
        btnViewAllHistory = findViewById(R.id.btnViewAllHistory);
    }

    private void setupRecyclerView() {
        historyAdapter = new ExpenseHistoryAdapter(expenseHistory, this::onHistoryItemClick);
        rvExpenseHistory.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseHistory.setAdapter(historyAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnExport.setOnClickListener(v -> exportData());
        
        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadExpenseData();
        });
        
        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadExpenseData();
        });
        
        btnViewAllHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryGroupsActivity.class);
            startActivity(intent);
        });
    }

    private void loadExpenseData() {
        // Get all split history
        List<SplitHistory> allHistory = databaseHelper.getAllSplitHistory();
        
        // Filter for current month
        expenseHistory = new ArrayList<>();
        String currentMonthStr = monthFormat.format(currentMonth.getTime());
        
        for (SplitHistory history : allHistory) {
            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Calendar historyCal = Calendar.getInstance();
                historyCal.setTime(dbFormat.parse(history.getCreatedAt()));
                
                String historyMonth = monthFormat.format(historyCal.getTime());
                if (historyMonth.equals(currentMonthStr)) {
                    expenseHistory.add(history);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Update adapter
        historyAdapter.updateData(expenseHistory);
        
        // Update summary
        updateSummary();
        
        // Update chart placeholder
        updateChart();
    }

    private void updateMonthDisplay() {
        tvCurrentMonth.setText(monthFormat.format(currentMonth.getTime()));
    }

    private void updateSummary() {
        double totalSpent = 0.0;
        int splitCount = expenseHistory.size();
        
        for (SplitHistory history : expenseHistory) {
            totalSpent += history.getFinalAmount();
        }
        
        tvTotalSpent.setText("$" + currencyFormat.format(totalSpent));
        tvSplitCount.setText(String.valueOf(splitCount));
    }

    private void updateChart() {
        if (expenseHistory.isEmpty()) {
            tvChartPlaceholder.setText("No data for this month");
        } else {
            // Simple chart data summary
            double total = 0;
            for (SplitHistory history : expenseHistory) {
                total += history.getFinalAmount();
            }
            double average = total / expenseHistory.size();
            
            tvChartPlaceholder.setText("Average per split: $" + currencyFormat.format(average) + 
                                    "\nTotal splits: " + expenseHistory.size());
        }
    }

    private void onHistoryItemClick(SplitHistory history) {
        // Navigate to result activity with historical data
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("BILL", history.getTotalAmount());
        intent.putExtra("TIP_PERCENT", history.getTipPercentage());
        intent.putExtra("PEOPLE", history.getPeopleCount());
        intent.putExtra("CURRENCY", history.getCurrencySymbol());
        intent.putExtra("FROM_HISTORY", true);
        intent.putExtra("HISTORY_ID", history.getId());
        
        startActivity(intent);
    }

    private void exportData() {
        if (expenseHistory.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create PDF file
            String monthName = "Unknown_Month";
            try {
                monthName = monthFormat.format(currentMonth);
            } catch (Exception e) {
                monthName = "Unknown_Month";
            }
            String fileName = "Expense_History_" + monthName.replace(" ", "_") + ".pdf";
            File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BillSplit");
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            File pdfFile = new File(downloadsDir, fileName);
            
            // Create simple HTML-based PDF content
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head>");
            htmlContent.append("<meta charset='UTF-8'>");
            htmlContent.append("<style>");
            htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            htmlContent.append("h1 { color: #5CC252; text-align: center; }");
            htmlContent.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            htmlContent.append("th { background-color: #f2f2f2; font-weight: bold; }");
            htmlContent.append("tr:nth-child(even) { background-color: #f9f9f9; }");
            htmlContent.append(".total { font-weight: bold; color: #5CC252; }");
            htmlContent.append("</style>");
            htmlContent.append("</head><body>");
            
            // Header
            htmlContent.append("<h1>Expense History Report</h1>");
            htmlContent.append("<p><strong>Period:</strong> ").append(monthName).append("</p>");
            htmlContent.append("<p><strong>Generated:</strong> ").append(new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new java.util.Date())).append("</p>");
            
            // Table
            htmlContent.append("<table>");
            htmlContent.append("<tr><th>Date</th><th>Description</th><th>Amount</th><th>Split Type</th><th>People</th></tr>");
            
            double grandTotal = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            
            for (SplitHistory expense : expenseHistory) {
                String date = "Unknown Date";
                try {
                    if (expense.getCreatedAt() != null && !expense.getCreatedAt().isEmpty()) {
                        // Try to parse the date string
                        java.util.Date parsedDate = inputDateFormat.parse(expense.getCreatedAt());
                        if (parsedDate != null) {
                            date = dateFormat.format(parsedDate);
                        }
                    }
                } catch (Exception e) {
                    // If parsing fails, use the raw string or default
                    date = expense.getCreatedAt() != null ? expense.getCreatedAt() : "Unknown Date";
                }
                
                String description = expense.getNotes() != null && !expense.getNotes().isEmpty() ? expense.getNotes() : "Untitled Split";
                double amount = expense.getFinalAmount();
                String splitType = expense.getSplitType();
                int peopleCount = expense.getPeopleCount();
                
                htmlContent.append("<tr>");
                htmlContent.append("<td>").append(date).append("</td>");
                htmlContent.append("<td>").append(description).append("</td>");
                htmlContent.append("<td>$").append(currencyFormat.format(amount)).append("</td>");
                htmlContent.append("<td>").append(splitType != null ? splitType.substring(0, 1).toUpperCase() + splitType.substring(1) : "Unknown").append("</td>");
                htmlContent.append("<td>").append(peopleCount).append("</td>");
                htmlContent.append("</tr>");
                
                grandTotal += amount;
            }
            
            htmlContent.append("</table>");
            
            // Summary
            htmlContent.append("<div style='margin-top: 20px; text-align: right;'>");
            htmlContent.append("<p><strong>Total Splits:</strong> ").append(expenseHistory.size()).append("</p>");
            htmlContent.append("<p class='total'><strong>Grand Total:</strong> $").append(currencyFormat.format(grandTotal)).append("</p>");
            htmlContent.append("</div>");
            
            htmlContent.append("</body></html>");
            
            // Write HTML to file (as PDF)
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(htmlContent.toString().getBytes());
            fos.close();
            
            // Share the file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/html");
            
            // Use FileProvider to get content URI
            android.net.Uri uri = FileProvider.getUriForFile(
                this, 
                getApplicationContext().getPackageName() + ".fileprovider", 
                pdfFile
            );
            
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Expense History - " + monthName);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here's your expense history for " + monthName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Expense History"));
            
            Toast.makeText(this, "PDF exported successfully! File saved to Downloads/BillSplit", Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadExpenseData();
    }
}
