package com.example.exercise2.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.exercise2.MainActivity;
import com.example.exercise2.R;

import androidx.navigation.Navigation;

public class MainFragment extends Fragment implements Button.OnClickListener {

    private MainViewModel mainViewModel;
    public static Context mContext;

    private static ImageButton buttonRecord_exercise; //운동 기록 버튼
    private static ImageButton buttonCommunity; //커뮤니티 버튼
    private static ImageButton buttonExercise; //운동 선택 버튼
    private TextView text_title1;
    private TextView text_title2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        setup(root);
        return root;
    }
    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.button_exercise:
                Navigation.findNavController(view).navigate(R.id.action_nav_main_to_nav_select);
                //((MainActivity)getActivity()).replaceFragment(SlideshowFragment.newInstance());
                break;
            case R.id.button_record_exercise:
                Navigation.findNavController(view).navigate(R.id.action_nav_main_to_nav_record);
                break;
            case R.id.button_community:
                Navigation.findNavController(view).navigate(R.id.action_nav_main_to_nav_community);
                break;
        }
    }

    public void setup(View view)
    {
        buttonExercise = (ImageButton)view.findViewById(R.id.button_exercise);
        buttonExercise.setOnClickListener(this);
        buttonRecord_exercise = (ImageButton)view.findViewById(R.id.button_record_exercise);
        buttonRecord_exercise.setOnClickListener(this);
        buttonCommunity = (ImageButton)view.findViewById(R.id.button_community);
        buttonCommunity.setOnClickListener(this);
        text_title1 = (TextView)view.findViewById(R.id.text_title1);
        text_title1.setText(((MainActivity)getActivity()).data_move("certifi_key")+"님 안녕하세요");
        text_title2 = (TextView)view.findViewById(R.id.text_title2);
        mContext=getActivity();
    }

    public void text_change(int check)
    {
        if(check == 1)
            text_title2.setVisibility(View.VISIBLE);
        else if(check == 0)
            text_title2.setVisibility(View.INVISIBLE);
    }
}