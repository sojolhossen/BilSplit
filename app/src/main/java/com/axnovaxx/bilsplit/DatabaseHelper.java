package com.axnovaxx.bilsplit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BillSplit.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_SPLIT_HISTORY = "split_history";
    private static final String TABLE_SAVED_GROUPS = "saved_groups";
    private static final String TABLE_GROUP_MEMBERS = "group_members";
    private static final String TABLE_SPLIT_ITEMS = "split_items";

    // Common column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";

    // Split History columns
    private static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    private static final String COLUMN_TIP_PERCENTAGE = "tip_percentage";
    private static final String COLUMN_TAX_AMOUNT = "tax_amount";
    private static final String COLUMN_PEOPLE_COUNT = "people_count";
    private static final String COLUMN_CURRENCY_SYMBOL = "currency_symbol";
    private static final String COLUMN_SPLIT_TYPE = "split_type"; // regular, custom, itemized
    private static final String COLUMN_NOTES = "notes";

    // Saved Groups columns
    private static final String COLUMN_GROUP_NAME = "group_name";

    // Group Members columns
    private static final String COLUMN_GROUP_ID = "group_id";
    private static final String COLUMN_MEMBER_NAME = "member_name";

    // Split Items columns (for itemized splits)
    private static final String COLUMN_SPLIT_ID = "split_id";
    private static final String COLUMN_ITEM_NAME = "item_name";
    private static final String COLUMN_ITEM_PRICE = "item_price";
    private static final String COLUMN_ITEM_QUANTITY = "item_quantity";
    private static final String COLUMN_ASSIGNED_TO = "assigned_to"; // JSON array of person names

    // Create table statements
    private static final String CREATE_TABLE_SPLIT_HISTORY = 
        "CREATE TABLE " + TABLE_SPLIT_HISTORY + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_TOTAL_AMOUNT + " REAL NOT NULL, " +
        COLUMN_TIP_PERCENTAGE + " REAL DEFAULT 0, " +
        COLUMN_TAX_AMOUNT + " REAL DEFAULT 0, " +
        COLUMN_PEOPLE_COUNT + " INTEGER NOT NULL, " +
        COLUMN_CURRENCY_SYMBOL + " TEXT DEFAULT '$', " +
        COLUMN_SPLIT_TYPE + " TEXT NOT NULL, " +
        COLUMN_NOTES + " TEXT, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")";

    private static final String CREATE_TABLE_SAVED_GROUPS = 
        "CREATE TABLE " + TABLE_SAVED_GROUPS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_GROUP_NAME + " TEXT NOT NULL UNIQUE, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")";

    private static final String CREATE_TABLE_GROUP_MEMBERS = 
        "CREATE TABLE " + TABLE_GROUP_MEMBERS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_GROUP_ID + " INTEGER NOT NULL, " +
        COLUMN_MEMBER_NAME + " TEXT NOT NULL, " +
        "FOREIGN KEY(" + COLUMN_GROUP_ID + ") REFERENCES " + TABLE_SAVED_GROUPS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
        ")";

    private static final String CREATE_TABLE_SPLIT_ITEMS = 
        "CREATE TABLE " + TABLE_SPLIT_ITEMS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_SPLIT_ID + " INTEGER NOT NULL, " +
        COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
        COLUMN_ITEM_PRICE + " REAL NOT NULL, " +
        COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 1, " +
        COLUMN_ASSIGNED_TO + " TEXT, " +
        "FOREIGN KEY(" + COLUMN_SPLIT_ID + ") REFERENCES " + TABLE_SPLIT_HISTORY + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
        ")";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SPLIT_HISTORY);
        db.execSQL(CREATE_TABLE_SAVED_GROUPS);
        db.execSQL(CREATE_TABLE_GROUP_MEMBERS);
        db.execSQL(CREATE_TABLE_SPLIT_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPLIT_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPLIT_HISTORY);
        onCreate(db);
    }

    // Split History operations
    public long insertSplitHistory(SplitHistory splitHistory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TOTAL_AMOUNT, splitHistory.getTotalAmount());
        values.put(COLUMN_TIP_PERCENTAGE, splitHistory.getTipPercentage());
        values.put(COLUMN_TAX_AMOUNT, splitHistory.getTaxAmount());
        values.put(COLUMN_PEOPLE_COUNT, splitHistory.getPeopleCount());
        values.put(COLUMN_CURRENCY_SYMBOL, splitHistory.getCurrencySymbol());
        values.put(COLUMN_SPLIT_TYPE, splitHistory.getSplitType());
        values.put(COLUMN_NOTES, splitHistory.getNotes());

        long splitId = db.insert(TABLE_SPLIT_HISTORY, null, values);

        // Insert items if it's an itemized split
        if (splitId != -1 && splitHistory.getItems() != null) {
            for (SplitItem item : splitHistory.getItems()) {
                insertSplitItem(splitId, item);
            }
        }

        return splitId;
    }

    public List<SplitHistory> getAllSplitHistory() {
        List<SplitHistory> splitHistoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_SPLIT_HISTORY + " ORDER BY " + COLUMN_CREATED_AT + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                SplitHistory splitHistory = new SplitHistory();
                splitHistory.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                splitHistory.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_AMOUNT)));
                splitHistory.setTipPercentage(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TIP_PERCENTAGE)));
                splitHistory.setTaxAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TAX_AMOUNT)));
                splitHistory.setPeopleCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PEOPLE_COUNT)));
                splitHistory.setCurrencySymbol(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY_SYMBOL)));
                splitHistory.setSplitType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPLIT_TYPE)));
                splitHistory.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)));
                splitHistory.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                
                // Get items for this split
                splitHistory.setItems(getSplitItemsForSplit(splitHistory.getId()));
                
                splitHistoryList.add(splitHistory);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return splitHistoryList;
    }

    // Saved Groups operations
    public long insertSavedGroup(SavedGroup group) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_GROUP_NAME, group.getName());
        
        long groupId = db.insert(TABLE_SAVED_GROUPS, null, values);
        
        // Debug logging
        Log.d("DatabaseHelper", "Inserting group: " + group.getName() + ", ID: " + groupId);

        // Insert members
        if (groupId != -1 && group.getMembers() != null) {
            for (String member : group.getMembers()) {
                Log.d("DatabaseHelper", "Inserting member: " + member + " for group: " + groupId);
                insertGroupMember(groupId, member);
            }
        }

        return groupId;
    }

    public int updateGroupMembers(long groupId, List<String> members) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Delete existing members
        int deletedRows = db.delete(TABLE_GROUP_MEMBERS, COLUMN_GROUP_ID + " = ?", new String[]{String.valueOf(groupId)});
        
        // Insert new members
        if (members != null) {
            for (String member : members) {
                insertGroupMember(groupId, member);
            }
        }
        
        Log.d("DatabaseHelper", "Updated group " + groupId + " with " + (members != null ? members.size() : 0) + " members");
        return deletedRows;
    }

    public List<SavedGroup> getAllSavedGroups() {
        List<SavedGroup> groupsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_SAVED_GROUPS + " ORDER BY " + COLUMN_GROUP_NAME;
        Cursor cursor = db.rawQuery(query, null);
        
        Log.d("DatabaseHelper", "Querying groups, cursor count: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                SavedGroup group = new SavedGroup();
                group.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                group.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)));
                group.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                
                // Get members for this group
                group.setMembers(getGroupMembers(group.getId()));
                
                Log.d("DatabaseHelper", "Loaded group: " + group.getName() + ", Members: " + group.getMemberCount());
                
                groupsList.add(group);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        Log.d("DatabaseHelper", "Total groups loaded: " + groupsList.size());
        return groupsList;
    }

    // Helper methods
    private long insertSplitItem(long splitId, SplitItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_SPLIT_ID, splitId);
        values.put(COLUMN_ITEM_NAME, item.getName());
        values.put(COLUMN_ITEM_PRICE, item.getPrice());
        values.put(COLUMN_ITEM_QUANTITY, item.getQuantity());
        values.put(COLUMN_ASSIGNED_TO, item.getAssignedTo());

        return db.insert(TABLE_SPLIT_ITEMS, null, values);
    }

    private List<SplitItem> getSplitItemsForSplit(long splitId) {
        List<SplitItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_SPLIT_ITEMS + " WHERE " + COLUMN_SPLIT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(splitId)});

        if (cursor.moveToFirst()) {
            do {
                SplitItem item = new SplitItem();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ITEM_PRICE)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY)));
                item.setAssignedTo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ASSIGNED_TO)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    private long insertGroupMember(long groupId, String memberName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_GROUP_ID, groupId);
        values.put(COLUMN_MEMBER_NAME, memberName);

        return db.insert(TABLE_GROUP_MEMBERS, null, values);
    }

    private List<String> getGroupMembers(long groupId) {
        List<String> members = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT " + COLUMN_MEMBER_NAME + " FROM " + TABLE_GROUP_MEMBERS + 
                      " WHERE " + COLUMN_GROUP_ID + " = ? ORDER BY " + COLUMN_MEMBER_NAME;
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(groupId)});

        if (cursor.moveToFirst()) {
            do {
                members.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    // Delete operations
    public int deleteSplitHistory(long splitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SPLIT_HISTORY, COLUMN_ID + " = ?", new String[]{String.valueOf(splitId)});
    }

    public int deleteSavedGroup(long groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SAVED_GROUPS, COLUMN_ID + " = ?", new String[]{String.valueOf(groupId)});
    }
}
