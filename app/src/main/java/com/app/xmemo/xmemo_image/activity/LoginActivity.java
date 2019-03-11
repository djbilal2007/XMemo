package com.app.xmemo.xmemo_image.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText edit_email, edit_pass;
    private TextView txt_forgotPwd, txt_signup;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Typeface typeface = Utils.setFont(this);

        String email = getIntent().getStringExtra("email");

        edit_email = (EditText) findViewById(R.id.edit_email);
        edit_email.setTypeface(typeface);

        edit_pass = (EditText) findViewById(R.id.edit_password);
        edit_pass.setTypeface(typeface);

        txt_forgotPwd = (TextView) findViewById(R.id.txt_forgot_password);
        txt_forgotPwd.setTypeface(typeface);

        txt_signup = (TextView) findViewById(R.id.txt_signup);
        txt_signup.setTypeface(typeface);

        btn_login = (Button)findViewById(R.id.btn_login);
        btn_login.setTypeface(typeface);

        btn_login.setOnClickListener(this);
        txt_forgotPwd.setOnClickListener(this);
        txt_signup.setOnClickListener(this);

        if(!TextUtils.isEmpty(email)){
            edit_email.setText(email);
        }
    }

    @Override
    public void onClick(View v) {
        if(v == btn_login){
            login();
        }
        if(v == txt_forgotPwd){
            goToForgotPasswordActivity();
        }
        if(v == txt_signup){
            goToRegisterActivity();
        }
    }

    private void login() {
        Utils.initLoading(this);

        String email = edit_email.getText().toString().trim();
        String pass = edit_pass.getText().toString().trim();

        email = email.toLowerCase();

        if(TextUtils.isEmpty(email)){
            Utils.showToast(this, "Please enter email");
        }else if(TextUtils.isEmpty(pass)){
            Utils.showToast(this, "Please enter password");
        }else {
            Utils.showLoading("Logging in.....");
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Utils.hideLoading();
                                Log.d(TAG,"Login Successfull");
                                startActivity(new Intent(LoginActivity.this, UserHomeActivity.class));
                                finish();
                            }else {
                                Utils.hideLoading();
                                Log.d(TAG,"Invalid Login");
                                Utils.showToast(LoginActivity.this, "Login Failed");
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.hideLoading();
                            Log.d(TAG,"Exception: " + e.getMessage());
                        }
                    });
        }
    }

    private void goToRegisterActivity() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void goToForgotPasswordActivity() {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    @Override
    public void onBackPressed() {
        Utils.exitApp(this);
    }
}
