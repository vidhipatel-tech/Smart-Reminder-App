package com.example.smartreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESC = "description";
    private static final String COL_DATE = "deadline"; // date part (dd/MM/yyyy)
    private static final String COL_TIME = "time";     // time part (HH:mm)

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DESC + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_TIME + " TEXT)";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // Insert task - returns inserted row id
    public long addTask(String title, String description, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_DESC, description);
        cv.put(COL_DATE, date);
        cv.put(COL_TIME, time);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_ID + "," + COL_TITLE + "," + COL_DESC + "," + COL_DATE + "," + COL_TIME +
                " FROM " + TABLE + " ORDER BY " + COL_ID + " DESC", null);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(0);
                String title = c.getString(1);
                String desc = c.getString(2);
                String date = c.getString(3);
                String time = c.getString(4);
                list.add(new Task(id, title, desc, date, time));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int updateTask(int id, String title, String description, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_DESC, description);
        cv.put(COL_DATE, date);
        cv.put(COL_TIME, time);
        int rows = db.update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
}
