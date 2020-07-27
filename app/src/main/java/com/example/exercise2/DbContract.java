package com.example.exercise2;

import android.provider.BaseColumns;

import java.util.Date;

public final class DbContract {
    private DbContract(){

    }

    public static class DbEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "db";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_COUNT = "count";

    }
}
