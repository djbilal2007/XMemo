package com.app.xmemo.xmemo_image.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.bean.User;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;
import com.app.xmemo.xmemo_image.utils.CircleImageView;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.HashMap;
import java.util.Map;


public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView profile_pic, pencil_icon;
    private EditText edit_name;
    private TextView txt_email;
    private Button btn_update;
    private ImageView back_arrow;


    private int CAMERA_REQUEST = 100;
    private int PICK_IMAGE_REQUEST = 101;

    private MarshMallowPermission marshMallowPermission;
    private static final String TAG = EditProfileActivity.class.getSimpleName();

    private DatabaseReference databaseRef;
    private static Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        databaseRef = FirebaseDatabase.getInstance().getReference("userList");

        Utils.initLoading(this);

        Typeface typeface = Utils.setFont(this);

        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String imgUrl = getIntent().getStringExtra("photoUrl");

        profile_pic = (CircleImageView)findViewById(R.id.img_edit_profile);
        profile_pic.setOnClickListener(this);

        pencil_icon = (CircleImageView)findViewById(R.id.blue_pencil_icon);
        pencil_icon.setOnClickListener(this);

        edit_name = (EditText)findViewById(R.id.edit_fullName_edit_profile);
        edit_name.setTypeface(typeface);
        
        txt_email = (TextView)findViewById(R.id.txt_email_edit_profile);
        txt_email.setTypeface(typeface);

        back_arrow = (ImageView)findViewById(R.id.back_arrow_profile);
        back_arrow.setOnClickListener(this);

        btn_update = (Button)findViewById(R.id.btn_update_edit_profile);
        btn_update.setTypeface(typeface);
        btn_update.setOnClickListener(this);

        Glide.with(this)
                .load(imgUrl)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profile_pic);
        txt_email.setText(email);
        edit_name.setText(name);
    }

    @Override
    public void onClick(View v) {
        if(v == btn_update){
            updateProfile();
        }
        if(v == back_arrow){
            onBackPressed();
        }
        if(v == profile_pic){
            checkPermissionForReadAndCamera();
        }
        if(v == pencil_icon){
            checkPermissionForReadAndCamera();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageURI = data.getData();
            Log.d(TAG,"Uri Path: " + imageURI.getPath());
            profile_pic.setImageURI(imageURI);
        }

        if (requestCode == CAMERA_REQUEST) {
            try {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {
                            profile_pic.setImageBitmap(bitmap);
                            imageURI = Utils.getImageUri(this, bitmap);
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
    
    private void updateProfile() {
        String fullName = edit_name.getText().toString().trim();

        if(TextUtils.isEmpty(fullName)){
            Utils.showToast(this, "Please enter fullname");
        }else {
            updateProfileInFirebase(fullName, imageURI);
        }
    }

    private void updateProfileInFirebase(String fullName, Uri imageUri) {
        Utils.showLoading("Updating...");
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updateProfile(profileChangeRequest);

        if(imageUri == null){
            updateNameInDatabase(fullName, user);
        }else {
            updateImageInFirebaseStorage(imageUri, fullName, user);
        }
    }

    private void updateNameInDatabase(final String fullName, final FirebaseUser user) {
        databaseRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                User updatedUser = new User(currentUser.getUid(), fullName, currentUser.getEmail());
                updatedUser.setProfileImage(currentUser.getProfileImage());

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(dataSnapshot.getKey(), updatedUser);
                databaseRef.updateChildren(childUpdates);
                Utils.hideLoading();
                Utils.showToast(EditProfileActivity.this, "Profile Updated Successfully");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
                Utils.showToast(EditProfileActivity.this, "Exception Occured");
            }
        });
    }

    private void updateImageInFirebaseStorage(Uri imageUri, final String fullName, final FirebaseUser user) {
        if(imageUri != null) {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference riversRef = storageReference.child(user.getUid() + "/profile_pic/" + "ProfilePic_" + System.currentTimeMillis());
            UploadTask uploadTask = riversRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG, downloadUrl.toString());
                   updateImageIntoDatabase(downloadUrl.toString(), fullName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.hideLoading();
                    Log.d(TAG, "Exception: " + e.getMessage());
                    Utils.showToast(EditProfileActivity.this, "Upload Failed");
                }
            });
        }else {
            Utils.hideLoading();
            Utils.showToast(this, "Please select a profile picture");
            Utils.showToast(this, "No File Path found");
        }
    }

    private void updateImageIntoDatabase(final String imgUrl, final String fullName) {
        databaseRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                User updatedUser = new User(currentUser.getUid(), fullName, currentUser.getEmail());
                updatedUser.setProfileImage(imgUrl);

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(dataSnapshot.getKey(), updatedUser);
                databaseRef.updateChildren(childUpdates);
                Utils.hideLoading();
                Utils.showToast(EditProfileActivity.this, "Profile Updated Successfully");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
                Utils.showToast(EditProfileActivity.this, "Exception Occured");
            }
        });
    }
}
