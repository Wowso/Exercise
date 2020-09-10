package com.example.exercise2.ui.exercise;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Exercise_Select_ViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public Exercise_Select_ViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}