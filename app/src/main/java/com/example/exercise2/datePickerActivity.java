package com.example.exercise2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class datePickerActivity extends AppCompatActivity {

    private int mYear =0, mMonth =0, mDay =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_date_picker);
        Intent intent = getIntent();

        mYear = intent.getIntExtra("mYear",0);
        mMonth = intent.getIntExtra("mMonth",0)-1;
        mDay = intent.getIntExtra("mDay",0);
        if(mYear == 0)
        {
            Calendar calendar = new GregorianCalendar();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = (calendar.get(Calendar.MONTH));
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
        }
        DatePicker datePicker = findViewById(R.id.vDatePicker);
        datePicker.init(mYear, mMonth, mDay, mOnDateChangedListener);
    }

    public void mOnClick(View v)
    {
        Intent intent = new Intent();
        intent.putExtra("mYear", mYear);
        intent.putExtra("mMonth", mMonth+1);
        intent.putExtra("mDay",mDay);
        setResult(RESULT_OK, intent);
        finish();
    }

    DatePicker.OnDateChangedListener mOnDateChangedListener = new DatePicker.OnDateChangedListener() {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
        }
    };
}
