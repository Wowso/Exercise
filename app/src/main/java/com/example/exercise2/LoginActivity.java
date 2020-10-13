package com.example.exercise2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.exercise2.databinding.ActivityLoginBinding;
import com.example.exercise2.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends BaseActivity implements Button.OnClickListener {

    private static final String TAG = "LoginActivity";

    private static final int REQUEST_ACT = 1;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private Context mContext;
    private ArrayList<String> certifi_number = new ArrayList<String>(Arrays.asList("B001","B002","B003","B004","B005","B006","B007","B008","B009"));
    private static boolean certifi_boolean = false;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setup();
        login_check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
            Toast.makeText(LoginActivity.this,"오류 발생",Toast.LENGTH_SHORT).show();
            return;
        }

        if(requestCode == REQUEST_ACT)
        {
            String result_Email = data.getStringExtra("result_email");
            String result_Password = data.getStringExtra("result_password");
            binding.fieldEmail.setText(result_Email);
            binding.fieldPassword.setText(result_Password);

            Toast.makeText(LoginActivity.this,"회원가입 완료",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(LoginActivity.this,"오류 발생",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_signup:
                intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivityForResult(intent,REQUEST_ACT);
                break;
            case R.id.btn_login:
                signIn();
                break;
            case R.id.checkBox:
                break;
            default:
                break;
        }
    } //버튼 입력 처리

    private void setup() {
        binding.btnLogin.setOnClickListener(this);
        binding.btnSignup.setOnClickListener(this);
        binding.checkBox.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        setProgressBar(R.id.progressBar_login);
        mContext = this;
    } //객체 초기화

    private void login_check() {
        String text_device = PreferenceManager.getString(mContext,"certifi_key");
        String text_email = PreferenceManager.getString(mContext,"email_key");
        String text_password = PreferenceManager.getString(mContext, "password_key");
        if(text_device !="")
        {
            binding.checkBox.setChecked(true);
        }
        binding.editTextTextPassword.setText(text_device);
        binding.fieldEmail.setText(text_email);
        binding.fieldPassword.setText(text_password);
    } //저장된 값을 불러와서 값이 있는지 없는지 확인

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }

        showProgressBar();
        String email = binding.fieldEmail.getText().toString();
        String password = binding.fieldPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressBar();

                        if (task.isSuccessful()) {
                            if(binding.checkBox.isChecked()) {
                                PreferenceManager.setString(mContext,"email_key",binding.fieldEmail.getText().toString());
                                PreferenceManager.setString(mContext,"password_key",binding.fieldPassword.getText().toString());
                                PreferenceManager.setString(mContext,"certifi_key",binding.editTextTextPassword.getText().toString());
                            }
                            else{ //checkbox가 해제되어있으면 저장된 내용 삭제
                                PreferenceManager.clear(mContext);
                            }
                            onAuthSuccess(task.getResult().getUser());
                        } else {

                            Toast.makeText(LoginActivity.this, "Sign In Failed"+"\n"+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("email_key",binding.fieldEmail.getText().toString());
        intent.putExtra("certifi_key",binding.editTextTextPassword.getText().toString());
        startActivity(intent);
        finish();
    }

    private boolean validateForm() {
        boolean result = false;

        for(int i=0;i<certifi_number.size();i++)
        {
            if(binding.editTextTextPassword.getText().toString().equals(certifi_number.get(i)))
            {
                result = true;
            }
        }
        if(!result) {binding.editTextTextPassword.setError("device Num Error");}

        if (TextUtils.isEmpty(binding.fieldEmail.getText().toString())) {
            binding.fieldEmail.setError("Required");
            result = false;
        } else {
            if(!binding.fieldEmail.getText().toString().contains("@"))
            {
                binding.fieldEmail.setError("이메일 형식을 확인하세요.");
            }
            else
            {
                binding.fieldEmail.setError(null);
            }
        }

        if (TextUtils.isEmpty(binding.fieldPassword.getText().toString())) {
            binding.fieldPassword.setError("Required");
            result = false;
        } else {
            if(binding.fieldPassword.getText().toString().length()<6)
            {
                binding.fieldPassword.setError("패스워드를 6자리 이상으로 입력하세요.");
            }
            else
            {
                binding.fieldPassword.setError(null);
            }
        }

        return result;
    }


}