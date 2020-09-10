package com.example.exercise2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements Button.OnClickListener {

    private Button btn_next;
    private EditText edit_password;
    private CheckBox cb_login;
    private Context mContext;
    private ArrayList<String> certifi_number = new ArrayList<String>(Arrays.asList("B001","B002","B003","B004","B005","B006","B007","B008","B009"));
    private static boolean certifi_boolean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setup();
        login_check();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_next:
                password_check(); //password 맞는지 비교
                if(certifi_boolean) {
                    CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox);
                    if(checkBox.isChecked()) //checkbox가 체크되어있을때 현재값 저장
                    {
                        PreferenceManager.setString(mContext,"certifi_key",edit_password.getText().toString());
                    }
                    else{ //checkbox가 해제되어있으면 저장된 내용 삭제
                        PreferenceManager.clear(mContext);
                    }
                    intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("certifi_key",edit_password.getText().toString());
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(this, "인증번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.checkBox:
                break;
            default:
                break;
        }
    } //버튼 입력 처리

    private void setup() {
        btn_next = (Button)findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
        cb_login = (CheckBox)findViewById(R.id.checkBox);
        cb_login.setOnClickListener(this);
        edit_password = (EditText)findViewById(R.id.editTextTextPassword);
        mContext = this;
    } //객체 초기화

    private void login_check() {
        String text = PreferenceManager.getString(mContext,"certifi_key");
        if(text !="")
        {
            cb_login.setChecked(true);
        }
        edit_password.setText(text);
    } //저장된 값을 불러와서 값이 있는지 없는지 확인

    private void password_check() {
        for(int i=0;i<certifi_number.size();i++)
        {
            if(edit_password.getText().toString().equals(certifi_number.get(i)))
            {
                certifi_boolean=true;
            }
        }
    } //패스워드 비교


}