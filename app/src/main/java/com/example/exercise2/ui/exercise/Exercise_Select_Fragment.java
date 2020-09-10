package com.example.exercise2.ui.exercise;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.exercise2.R;

public class Exercise_Select_Fragment extends Fragment implements Button.OnClickListener {

    private Exercise_Select_ViewModel exerciseViewModel;
    private RadioButton rg_btn1;
    private RadioButton rg_btn2;
    private RadioButton rg_btn3;

    private ImageButton btn_next;
    private boolean btn_next_check = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        exerciseViewModel = ViewModelProviders.of(this).get(Exercise_Select_ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_exercise_select, container, false);

        setup(root);
        return root;
    }

    public void setup(View view)
    {
        btn_next = (ImageButton)view.findViewById(R.id.next_button);
        btn_next.setOnClickListener(this);
        rg_btn1 = (RadioButton)view.findViewById(R.id.rg_btn1);
        rg_btn2 = (RadioButton)view.findViewById(R.id.rg_btn2);
        rg_btn3 = (RadioButton)view.findViewById(R.id.rg_btn3);
        rg_btn1.setOnClickListener(this);
        rg_btn2.setOnClickListener(this);
        rg_btn3.setOnClickListener(this);

    }
    @Override
    public void onClick(View view){
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.next_button:
                if(btn_next_check)
                {
                    if(rg_btn1.isChecked()) {
                        bundle.putInt("max_count",10);
                    }
                    else if(rg_btn2.isChecked()) {
                        bundle.putInt("max_count",15);
                    }
                    else if(rg_btn3.isChecked()) {
                        bundle.putInt("max_count",20);
                    }
                    Navigation.findNavController(view).navigate(R.id.action_nav_select_to_exerciseFragment,bundle);
                }
                break;
            case R.id.rg_btn1:
            case R.id.rg_btn2:
            case R.id.rg_btn3:
                btn_next.setImageResource(R.drawable.btn_next1);
                btn_next_check = true;
                break;
        }
    }

}