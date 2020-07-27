package com.example.exercise2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class Select_exercise extends AppCompatActivity implements Button.OnClickListener {

    private Button bu_Exercise1;
    private Button bu_Exercise2;
    private Button bu_Exercise3;
    private Button bu_Exercise4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_exercise);
        bu_Exercise1 = (Button)findViewById(R.id.bu_exercise1);
        bu_Exercise2 = (Button)findViewById(R.id.bu_exercise2);
        bu_Exercise3 = (Button)findViewById(R.id.bu_exercise3);
        bu_Exercise4 = (Button)findViewById(R.id.bu_exercise4);
        bu_Exercise1.setOnClickListener(this);
        bu_Exercise2.setOnClickListener(this);
        bu_Exercise3.setOnClickListener(this);
        bu_Exercise4.setOnClickListener(this);


    }
    @Override
    public void onClick(View view){
        Intent intent;
        switch (view.getId()) {
            case R.id.bu_exercise1:
                intent = new Intent(Select_exercise.this, Exercise.class);
                intent.putExtra("max_count",10);
                startActivity(intent);
                break;
            case R.id.bu_exercise2:
                intent = new Intent(Select_exercise.this, Exercise.class);
                intent.putExtra("max_count",15);
                startActivity(intent);
                break;
            case R.id.bu_exercise3:
                intent = new Intent(Select_exercise.this, Exercise.class);
                intent.putExtra("max_count",20);
                startActivity(intent);
                break;
            case R.id.bu_exercise4:
                intent = new Intent(Select_exercise.this, Exercise.class);
                //intent.putExtra("max_count",30);
                startActivity(intent);
                break;
        }
    }

}
