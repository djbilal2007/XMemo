package com.app.xmemo.xmemo_image.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.bean.User;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;
import com.app.xmemo.xmemo_image.utils.CircleImageView;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private int CAMERA_REQUEST = 100;
    private int PICK_IMAGE_REQUEST = 101;

    private EditText edit_fullname, edit_email, edit_pwd, edit_conf_pwd;
    private Button btn_submit;
    private CircleImageView img_profile_pic, camera_icon;
    private ImageView back_arrow;
    private static Uri imageURI;

    private MarshMallowPermission marshMallowPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Utils.initLoading(this);

        Typeface typeface = Utils.setFont(this);

        marshMallowPermission = new MarshMallowPermission(this);

        back_arrow = (ImageView)findViewById(R.id.back_arrow_register);
        back_arrow.setOnClickListener(this);

        edit_fullname = (EditText) findViewById(R.id.edit_fullname_register);
        edit_fullname.setTypeface(typeface);

        edit_email = (EditText) findViewById(R.id.edit_email_register);
        edit_email.setTypeface(typeface);

        edit_pwd = (EditText) findViewById(R.id.edit_pwd_register);
        edit_pwd.setTypeface(typeface);

        edit_conf_pwd = (EditText) findViewById(R.id.edit_confirmpwd_register);
        edit_conf_pwd.setTypeface(typeface);

        img_profile_pic = (CircleImageView)findViewById(R.id.img_profile_pic);
        camera_icon = (CircleImageView)findViewById(R.id.camera_icon);

        btn_submit = (Button)findViewById(R.id.btn_submit_register);
        btn_submit.setTypeface(typeface);

        img_profile_pic.setOnClickListener(this);
        camera_icon.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == btn_submit){
            checkAllFields();
            //checkEmailAlreadyExist("djbilal2007@gmail.com");
        }
        if(v == img_profile_pic){
            checkPermissionForReadAndCamera();
        }
        if(v == camera_icon){
            checkPermissionForReadAndCamera();
        }
        if(v == back_arrow){
            onBackPressed();
        }
    }

    private void checkPermissionForReadAndCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!marshMallowPermission.checkPermissionForCamera()) {
                marshMallowPermission.requestPermissionForCamera();
            } else if (!marshMallowPermission.checkPermissionForReadStorage()) {
                marshMallowPermission.requestPermissionForReadStorage();
            } else if (!marshMallowPermission.checkPermissionForExternalStorage()) {
                marshMallowPermission.requestPermissionForExternalStorage();
            } else {
                chooseImage();
            }
        }else {
            chooseImage();
        }
    }

    private void chooseImage() {
        String[] items = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select From");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);

                } else if (which == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /*switch (requestCode){
            case MarshMallowPermission.CAMERA_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    captureImage();
                }
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageURI = data.getData();
            Log.d(TAG,"Uri Path: " + imageURI.getPath());
            img_profile_pic.setImageURI(imageURI);
        }

        if (requestCode == CAMERA_REQUEST) {
            try {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {
                            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 120,120, false);
                            img_profile_pic.setImageBitmap(newBitmap);
                            imageURI = Utils.getImageUri(this, newBitmap);
                            Log.d(TAG, "Uri Path : " + imageURI);
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Exception: " + e.getMessage());
            }
        }
    }



    private void checkAllFields() {
        String fullName = "", email = "", pass = "", conf_pwd = "";

        fullName = edit_fullname.getText().toString().trim();
        email = edit_email.getText().toString().trim();
        pass = edit_pwd.getText().toString().trim();
        conf_pwd = edit_conf_pwd.getText().toString().trim();

        email = email.toLowerCase();

        if(TextUtils.isEmpty(fullName)){
            Utils.showToast(this, "Please enter fullname");
        }else if(TextUtils.isEmpty(email)){
            Utils.showToast(this, "Please enter email");
        }else if(!Utils.isValidEmailAddress(email)){
            Utils.showToast(this, "Please enter valid email address");
        }else if(TextUtils.isEmpty(pass)){
            Utils.showToast(this, "Please enter password");
        }else if(pass.length() < 6){
            Utils.showToast(this, "Password must be atleast 6-20 characters long");
        }else if(!Utils.isValidPassword(pass)){
            Utils.showToast(this, "Password must contain a letter, a number and a special symbol");
        }else if(!pass.equals(conf_pwd)){
            Utils.showToast(this, "Passwords do not match");
        }else if(imageURI == null){
            Utils.showToast(this, "Please select a profile picture");
        }else {
            checkEmailInFirebase(fullName, email, pass);
        }
    }

    boolean isEmailExist;
    private void checkEmailInFirebase(final String fullName, final String email, final String pass) {
        isEmailExist = false;
        Utils.showLoading("Registering....");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("userList");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (email.equals(user.getEmail())) {
                        isEmailExist = true;
                        break;
                    }
                }

                if (isEmailExist) {
                    Utils.hideLoading();
                    Utils.showToast(RegisterActivity.this, "Email-Id already registered.Please choose different one");
                    return;
                } else {
                    registerUser(fullName, email, pass);
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Exception: " + databaseError.getMessage());
                Utils.showToast(RegisterActivity.this, "Exception Occured");
                Utils.hideLoading();
            }
        });
    }

    private void registerUser(final String fullName, final String email, String pass) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Successful Registered");
                            FirebaseUser firebaseUser = task.getResult().getUser();

                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .setPhotoUri(imageURI)
                                    .build();

                            firebaseUser.updateProfile(profileChangeRequest);
                            User user = new User(firebaseUser.getUid(), fullName, email);
                            saveImageToFirebase(imageURI, user);
                        }else {
                            Utils.hideLoading();
                            Log.d(TAG, "Registeration Failed");
                            Utils.showToast(RegisterActivity.this, "Registeration Failed");
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
                Utils.hideLoading();
                Utils.showToast(RegisterActivity.this, "Exception Occured");
            }
        });
    }

    private void saveImageToFirebase(Uri imageUri, final User user) {
        if(imageUri != null) {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference riversRef = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile_pic/" + "ProfilePic_" + System.currentTimeMillis());
            UploadTask uploadTask = riversRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Utils.hideLoading();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG, downloadUrl.toString());
                    user.setProfileImage(downloadUrl.toString());
                    storeUserToDatabase(user);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.hideLoading();
                    Log.d(TAG, "Exception: " + e.getMessage());
                    Utils.showToast(RegisterActivity.this, "Upload Failed");
                }
            });
        }else {
            Utils.hideLoading();
            Utils.showToast(this, "Please select a profile picture");
            Utils.showToast(this, "No File Path found");
        }
    }

    private void storeUserToDatabase(User user) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("userList")
                .child(user.getUid())
                .setValue(user)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FirebaseAuth.getInstance().signOut();
                        }
                    }
                });

        Log.d(TAG, "Registration Successful");
        Utils.showToast(this, "Registration Successful");

        edit_fullname.setText("");
        edit_email.setText("");
        edit_pwd.setText("");
        edit_conf_pwd.setText("");

        Utils.hideLoading();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
        finish();
    }
}
