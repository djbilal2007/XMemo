package com.app.xmemo.xmemo_image.activity;

import android.graphics.Typeface;
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
import com.app.xmemo.xmemo_image.bean.User;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edit_email;
    private Button btn_reset;
    private ImageView back_arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Utils.initLoading(this);

        Typeface typeface = Utils.setFont(this);

        back_arrow = (ImageView)findViewById(R.id.back_arrow_forgot);
        back_arrow.setOnClickListener(this);

        edit_email = (EditText) findViewById(R.id.edit_email_forgot_pwd);
        edit_email.setTypeface(typeface);

        btn_reset = (Button) findViewById(R.id.btn_reset_pwd);
        btn_reset.setTypeface(typeface);
        btn_reset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == btn_reset){
            checkField();
        }
        if(v == back_arrow){
            onBackPressed();
        }
    }


    private void checkField() {
        String email = edit_email.getText().toString().trim();
        email = email.toLowerCase();

        if(TextUtils.isEmpty(email)){
            Utils.showToast(this, "Please enter email");
        }else if(!Utils.isValidEmailAddress(email)){
            Utils.showToast(this, "Please enter valid email address");
        }else {
            checkEmailInFirebase(email);
            //resetPassword(email);
        }
    }
    boolean isEmailExist;
    private void checkEmailInFirebase(final String email) {
        isEmailExist = false;
        Utils.showLoading("Resetting Password....");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("userList");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (email.equals(user.getEmail())) {
                        isEmailExist = true;
                    }
                }

                if(isEmailExist){
                    resetPassword(email);
                }else {
                    Utils.hideLoading();
                    Utils.showToast(ForgotPasswordActivity.this, "Email not registered");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ForgotPassword", "Exception: " + databaseError.getMessage());
                Utils.showToast(ForgotPasswordActivity.this, "Exception Occured");
                Utils.hideLoading();
            }
        });
    }

    private void resetPassword(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Utils.hideLoading();
                            Log.d("ForgotPassword", "Email Sent");
                            Utils.showToast(ForgotPasswordActivity.this, "Password Reset Link sent. Please check email.");
                            edit_email.setText("");
                        }else {
                            Utils.hideLoading();
                            Log.d("ForgotPassword", "Email Sent Failed");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.hideLoading();
                Log.d("ForgotPassword", "Exception occured: " + e.getMessage());
                Utils.showToast(ForgotPasswordActivity.this, "Exception Occured");
            }
        });
    }
}
