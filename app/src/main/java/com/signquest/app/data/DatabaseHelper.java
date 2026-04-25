package com.signquest.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "signquest.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_COMPREHENSION = "comprehension_results";
    public static final String COL_ID = "_id";
    public static final String COL_PROFILE_NAME = "profile_name";
    public static final String COL_SIGN_KEY = "sign_key";
    public static final String COL_PASSED = "passed"; // 1 or 0
    public static final String COL_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_COMPREHENSION + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PROFILE_NAME + " TEXT, " +
                COL_SIGN_KEY + " TEXT, " +
                COL_PASSED + " INTEGER, " +
                COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPREHENSION);
        onCreate(db);
    }

    public void logComprehensionResult(String profileName, String signKey, boolean passed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_NAME, profileName);
        values.put(COL_SIGN_KEY, signKey);
        values.put(COL_PASSED, passed ? 1 : 0);
        db.insert(TABLE_COMPREHENSION, null, values);
        db.close();
    }
}
