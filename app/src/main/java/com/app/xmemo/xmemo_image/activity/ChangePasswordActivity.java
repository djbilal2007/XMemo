package com.app.xmemo.xmemo_image.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;


public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView back_arrow;
    private Button update_pwd;
    private EditText edit_old, edit_new, edit_conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Utils.initLoading(this);

        edit_old = (EditText)findViewById(R.id.edit_old_pwd);
        edit_new = (EditText)findViewById(R.id.edit_new_pwd);
        edit_conf = (EditText)findViewById(R.id.edit_new_conf_pwd);

        update_pwd = (Button)findViewById(R.id.btn_update_pwd);
        update_pwd.setOnClickListener(this);

        back_arrow = (ImageView)findViewById(R.id.back_arrow_change_pwd);
        back_arrow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == back_arrow){
            onBackPressed();
        }
        if(v == update_pwd){
            updatePassword();
        }
    }

    private void updatePassword() {
        String oldPassword = edit_old.getText().toString().trim();
        AuthCredential authCredential = EmailAuthProvider.getCredential(FirebaseAuth.getInstance().getCurrentUser().getEmail(), oldPassword);
        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(authCredential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("ChangePassword", "Password is correct");
                            checkFieldToUpdatePassword();
                        }else {
                            Utils.showToast(ChangePasswordActivity.this, "Password is Incorrect");
                            Log.d("ChangePassword", "Password is Incorrect");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ChangePassword", "Exception: " + e.getMessage());
            }
        });
    }

    private void checkFieldToUpdatePassword() {
        String newPassword = edit_new.getText().toString().trim();
        String newPassword_conf = edit_conf.getText().toString().trim();

        if(TextUtils.isEmpty(newPassword)){
            Utils.showToast(this, "Please enter new password");
        }else if(newPassword.length() < 6){
            Utils.showToast(this, "Password must be atleast 6-20 characters long");
        }else if(!Utils.isValidPassword(newPassword)){
            Utils.showToast(this, "Password must contain a letter, a number and a special symbol");
        }else if(!newPassword.equals(newPassword_conf)){
            Utils.showToast(this, "Passwords do not match");
        }else{
            updatePasswordinFirebaseDatabase(newPassword);
        }
    }

    private void updatePasswordinFirebaseDatabase(String newPassword) {
        Utils.showLoading("Updating Password....");
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Utils.hideLoading();
                            Utils.showToast(ChangePasswordActivity.this, "Password updated successfully");
                            Log.d("ChangePassword", "Password updated successfully");
                            edit_old.setText("");
                            edit_new.setText("");
                            edit_conf.setText("");
                        }else {
                            Utils.hideLoading();
                            Log.d("ChangePassword", "Password do not updated");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.hideLoading();
                Log.d("ChangePassword", "Exception: " + e.getMessage());
            }
        });
    }
}
