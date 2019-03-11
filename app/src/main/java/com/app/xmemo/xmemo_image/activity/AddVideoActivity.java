package com.app.xmemo.xmemo_image.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.app.xmemo.xmemo_image.bean.Video;
import com.app.xmemo.xmemo_image.utils.Constants;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class AddVideoActivity extends AppCompatActivity implements View.OnClickListener {

    private GridView gridView;
    private AddImageAdapter adapter;
    private ImageButton send_vidBtn, add_vidBtn;
    private TextView txt_folder_name;

    private StorageReference storageReference;

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    private static String folder_name = "", folder_key = "";

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 102;
    private static String userUID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        //Upload Notification
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Uploading Videos")
                .setSmallIcon(Utils.getNotificationIcon());
        //

        folder_name = getIntent().getStringExtra("folder_name");
        folder_key = getIntent().getStringExtra("folder_key");

        txt_folder_name = (TextView)findViewById(R.id.folder_name_video_header);
        txt_folder_name.setText(folder_name);

        add_vidBtn = (ImageButton)findViewById(R.id.add_videoBtn);
        add_vidBtn.setOnClickListener(this);

        send_vidBtn = (ImageButton)findViewById(R.id.send_videoBtn);
        send_vidBtn.setOnClickListener(this);

        gridView = (GridView)findViewById(R.id.gridView_addVideo);
    }

    @Override
    public void onClick(View v) {
        if(v == send_vidBtn){
            uploadToServer();
        }
        if(v == add_vidBtn){
            chooseVideofromGallery();
        }
    }

    private void chooseVideofromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO);
        /*Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_TAKE_GALLERY_VIDEO:
                if(resultCode == RESULT_OK){
                    Uri selectedVideoUri = data.getData();
                    Log.d("AddVideoActivity", "Path: " + selectedVideoUri.toString());
                    Constants.videoUrlList.add(selectedVideoUri);
                    updateGridView();
                }
                break;
        }
    }

    private void updateGridView() {
        adapter = new AddImageAdapter (this, Constants.videoUrlList);
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //Upload to Firebase
    private boolean uploadToServer() {
        if (!Utils.isInternetConnected(this)) {
            Utils.showToast(this, getResources().getString(R.string.check_internet_connection));
            return false;
        } else {
            if (Constants.videoUrlList.isEmpty()) {
                Utils.showToast(this, "Please select some videos to upload");
            }else {
                syncToFirebase();
            }
            return true;
        }
    }

    int size = 0;
    int k = -1;
    long time;

    private void syncToFirebase() {
        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        size = Constants.videoUrlList.size();
        storageReference = FirebaseStorage.getInstance().getReference();
        Utils.showToast(AddVideoActivity.this, "Videos will upload in background");
        startNotificationInTray();

        createFolder();

        for(int i = 0; i< Constants.videoUrlList.size(); i++){
            Log.d("AddVideoActivity", "File " + i + ": " + Constants.videoUrlList.get(i).toString());

            String path = getRealPathFromUri(Constants.videoUrlList.get(i));
            final Uri uri = getImageUri(this, path);

            time = System.currentTimeMillis();
            final String fileName = "VID_" + time;

            StorageReference reference = storageReference.child(userUID + "/uploadedVideos/" + folder_key + "/" + fileName);
            reference.putFile(Constants.videoUrlList.get(i))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d("AddVideoActivity", "Download Url: " + downloadUrl.toString());
                            add();
                            saveThumbnailImage(fileName, downloadUrl.toString(), uri);
                            // Utility.hideLoading();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.showToast(AddVideoActivity.this, "Error Occured. " + e.getMessage());
                    Log.d("AddVideoActivity", "Exception: " + e.getMessage());
                }
            });
        }
        finish();
    }

    private void saveThumbnailImage(final String name, final String fileUrl, Uri uri) {

        String subFileName = name.split("_")[1];
        final String thumbnailName = "IMG_" + subFileName;

        StorageReference reference = storageReference.child(userUID + "/uploadedVideos/" + folder_key + "/thumbnails/" + thumbnailName);
        reference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("AddVideoActivity", "Download Url: " + downloadUrl.toString());
                        saveVideoToDatabase(name, fileUrl, downloadUrl.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.showToast(AddVideoActivity.this, "Error Occured. " + e.getMessage());
                        Log.d("AddVideoActivity", "Exception: " + e.getMessage());
                    }
                });
    }

    private void createFolder() {
        if(folder_key != null && !TextUtils.isEmpty(folder_key)){
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("uploadedData")
                    .child(userUID)
                    .child("uploadedVideos")
                    .child(folder_key)
                    .child("folderName")
                    .setValue(folder_name);
        }else {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("uploadedData")
                    .child(userUID)
                    .child("uploadedVideos")
                    .push();

            folder_key = ref.getKey();
            ref.child("folderName")
                    .setValue(folder_name);
        }
    }

    private void saveVideoToDatabase(String name, String fileUrl, String thumbnailUrl) {
        Video video = new Video(name, fileUrl, thumbnailUrl);

        FirebaseDatabase.getInstance()
                .getReference()
                .child("uploadedData")
                .child(userUID)
                .child("uploadedVideos")
                .child(folder_key)
                .child("videoData")
                .push()
                .setValue(video);
    }

    private void add() {
        k++;
        if(k == (size - 1)){
            stopNotificationProgress();
        }
    }

    private void stopNotificationProgress() {
        builder.setContentText("Videos Uploading Completed");
        builder.setProgress(0, 0, false);
        notificationManager.notify(0, builder.build());
    }

    private void startNotificationInTray() {
        builder.setProgress(0, 0, true);
        notificationManager.notify(0, builder.build());
    }

    private Uri getImageUri(Context inContext, String path) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String imagePath =  MediaStore.Images.Media.insertImage(inContext.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(imagePath);
    }

    private String getRealPathFromUri(Uri contentUri) {
        String [] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null,null);
        Cursor cursor = cursorLoader.loadInBackground();
        int colmn_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(colmn_index);
        cursor.close();
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.videoUrlList.clear();
    }
}
