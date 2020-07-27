package com.example.exercise2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper sInstance;

    private static final int DB_VERSION = 1;
    private static final String DB_NAME ="Exercise.db";
    private static final String SQL_CREATE_ENTRIES =
            String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER)",
                DbContract.DbEntry.TABLE_NAME,
                DbContract.DbEntry._ID,
                    DbContract.DbEntry.COLUMN_DATE,
                    DbContract.DbEntry.COLUMN_COUNT);

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DbContract.DbEntry.TABLE_NAME;

    public static DbHelper getInstance(Context context)
    {
        if(sInstance == null)
        {
            sInstance = new DbHelper(context);
        }
        return sInstance;
    }

    public DbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { db.execSQL(SQL_CREATE_ENTRIES);}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
