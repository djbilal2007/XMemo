package com.app.xmemo.xmemo_image.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.adapter.AddImageAdapter;
import com.app.xmemo.xmemo_image.bean.Image;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.define.Define;

import java.util.ArrayList;

import static com.app.xmemo.xmemo_image.utils.Constants.imageURIList;


public class AddImageActivity extends AppCompatActivity implements View.OnClickListener {

    private GridView gridView;
    private AddImageAdapter adapter;
    private ImageButton send_ImgBtn, add_imgBtn;
    private TextView txt_folder_name;

    private static String folder_name = "", folder_key = "";
    private static String userUID = "";


    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);


        //Upload Notification
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Uploading Images")
                .setSmallIcon(Utils.getNotificationIcon());
        //


        folder_name = getIntent().getStringExtra("folder_name");
        folder_key = getIntent().getStringExtra("folder_key");

        txt_folder_name = (TextView)findViewById(R.id.folder_name_header);
        txt_folder_name.setText(folder_name);

        send_ImgBtn = (ImageButton)findViewById(R.id.send_imgBtn);
        send_ImgBtn.setOnClickListener(this);

        add_imgBtn = (ImageButton)findViewById(R.id.add_imgActivityBtn);
        add_imgBtn.setOnClickListener(this);

        gridView = (GridView)findViewById(R.id.gridView_addImage);
    }

    @Override
    public void onClick(View v) {
        if(v == add_imgBtn){
            FishBun.with(this).startAlbum();
        }
        if(v == send_ImgBtn){
            uploadToServer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Define.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ArrayList<Uri> galleryImages = data.getParcelableArrayListExtra(Define.INTENT_PATH);
                    imageURIList.addAll(galleryImages);
                    updateGridView();
                }
                break;
        }
    }

    private void updateGridView() {
        adapter = new AddImageAdapter (this, imageURIList);
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //Upload to Firebase
    private boolean uploadToServer() {
        if (!Utils.isInternetConnected(this)) {
            Utils.showToast(this, getResources().getString(R.string.check_internet_connection));
            return false;
        } else {
            if (imageURIList.isEmpty()) {
                Utils.showToast(this, "Please select some images to upload");
            }else {
                syncToFirebase();
            }
            return true;
        }
    }
    int size = 0;
    int k = -1;
    private void syncToFirebase() {
        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        size = imageURIList.size();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Utils.showToast(AddImageActivity.this, "Images will upload in background");
        startNotificationInTray();

        createFolder();

        for(int i = 0; i< imageURIList.size(); i++){
            Log.d("GalleryFragment", "File " + i + ": " + imageURIList.get(i).toString());

            final String fileName = "IMG_" + System.currentTimeMillis();

            StorageReference reference = storageReference.child(userUID + "/uploadedImages/" + folder_key + "/" + fileName);
            reference.putFile(imageURIList.get(i))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d("GalleryFragment", "Download Url: " + downloadUrl.toString());
                            add();
                            saveImageUrlToDatabase(fileName, downloadUrl.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.showToast(AddImageActivity.this, "Error Occured. " + e.getMessage());
                    Log.d("GalleryFragment", "Exception: " + e.getMessage());
                }
            });
        }
        finish();
    }

    private void add() {
        k++;
        if(k == (size - 1)){
            stopNotificationProgress();
        }
    }

    private void stopNotificationProgress() {
        builder.setContentText("Images Uploading Completed");
        builder.setProgress(0, 0, false);
        notificationManager.notify(0, builder.build());
    }

    private void startNotificationInTray() {
        builder.setProgress(0, 0, true);
        notificationManager.notify(0, builder.build());
    }

    private void createFolder() {
        if(folder_key != null && !TextUtils.isEmpty(folder_key)){
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("uploadedData")
                    .child(userUID)
                    .child("uploadedImages")
                    .child(folder_key)
                    .child("folderName")
                    .setValue(folder_name);
        }else {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("uploadedData")
                    .child(userUID)
                    .child("uploadedImages")
                    .push();

            folder_key = ref.getKey();
            ref.child("folderName")
                    .setValue(folder_name);
        }
    }


    private void saveImageUrlToDatabase(String name, String downloadUrl) {
        Image image = new Image(name, downloadUrl);
        FirebaseDatabase.getInstance()
                .getReference()
                .child("uploadedData")
                .child(userUID)
                .child("uploadedImages")
                .child(folder_key)
                .child("imagesData")
                .push()
                .setValue(image);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageURIList.clear();
    }
}


