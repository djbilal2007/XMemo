package com.app.xmemo.xmemo_image.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.bean.Video;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;


public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();
    private VideoView videoView;
    private MediaController mc;

    private ImageButton share_vidBtn, download_vidBtn, delete_vidBtn, back_arrow;

    private static String videoUrl = "", videoName = "", thumbnailUrl = "", folderKey = "", userUID = "" ;
    private MarshMallowPermission marshMallowPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Utils.initLoading(this);

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        marshMallowPermission = new MarshMallowPermission(this);

        share_vidBtn= (ImageButton)findViewById(R.id.img_shareVidBtn);
        share_vidBtn.setOnClickListener(this);

        download_vidBtn = (ImageButton)findViewById(R.id.img_downloadVidBtn);
        download_vidBtn.setOnClickListener(this);

        delete_vidBtn = (ImageButton)findViewById(R.id.img_deleteVidBtn);
        delete_vidBtn.setOnClickListener(this);

        back_arrow = (ImageButton)findViewById(R.id.img_backArrowVideo);
        back_arrow.setOnClickListener(this);

        videoView = (VideoView)findViewById(R.id.videoView);

        thumbnailUrl = getIntent().getStringExtra("thumbnailUrl");
        videoUrl = getIntent().getStringExtra("videoUrl");
        videoName = getIntent().getStringExtra("videoName");

        folderKey = getIntent().getStringExtra("folder_key");

        Log.d("VideoPlayerActivity", "Thumbnail: " + thumbnailUrl);
        Log.d("VideoPlayerActivity", "Video: " + videoUrl);
        Log.d("VideoPlayerActivity", "VideoName: " + videoName);

        mc = new MediaController(VideoPlayerActivity.this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);

        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v == download_vidBtn){
            //downloadCurrentVideo();
            checkPermissionForStorage();
        }
        if(v == share_vidBtn){
            shareCurrentVideo();
        }
        if(v == delete_vidBtn){
            createAlertDialog();
        }
        if(v == back_arrow){
            onBackPressed();
        }
    }

    //Delete Video
    int pos;
    private void createAlertDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setMessage("Video will be permanently deleted. Are you sure?");
        build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //deleteCurrentVideo();
                deleteVideoFromDatabase();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        build.show();
    }

    private void deleteVideoFromDatabase() {
        Utils.showLoading("Deleting Video.....");
        final String fileName = videoName;

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedVideos/" + folderKey + "/videoData");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Video video= ds.getValue(Video.class);
                    if(video.getName().equals(fileName)){
                        Log.d("VideoPlayerActivity", "File Found");
                        Log.d("VideoPlayerActivity", "Key: " + ds.getKey());

                        databaseRef.child(ds.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("VideoPlayerActivity", "Data deleted from Database");
                                deleteThumbnailImage(fileName);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("VideoPlayerActivity", "Data deletion failed. Exception: " + e.getMessage());
                                Utils.hideLoading();
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("VideoPlayerActivity", "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
            }
        });
    }

    private void deleteThumbnailImage(final String fileName) {
        String subFileName = fileName.split("_")[1];
        String fullFileName = "IMG_" + subFileName;
        Log.d("VideoPlayerActivity", "Full File Name: " + fullFileName);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference reference = storageReference.child(userUID + "/uploadedVideos/" + folderKey + "/thumbnails/" + fullFileName);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("VideoPlayerActivity", "Thumbnail Deleted Succesfully");
                deleteCurrentVideoFromStorage(fileName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.hideLoading();
                Log.d("VideoPlayerActivity", "Exception: " + e.getMessage());
            }
        });

    }

    private void deleteCurrentVideoFromStorage(String fileName) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference reference = storageReference.child(userUID + "/uploadedVideos/" + folderKey + "/" + fileName);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("VideoPlayerActivity", "File Deleted Succesfully");
                Utils.hideLoading();
                Utils.showToast(VideoPlayerActivity.this, "Video Deleted Successfully");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("VideoPlayerActivity", "Exception: " + e.getMessage());
            }
        });
    }


    //Download Video
    private void checkPermissionForStorage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!marshMallowPermission.checkPermissionForExternalStorage()) {
                marshMallowPermission.requestPermissionForExternalStorage();
            } else {
                downloadCurrentVideo();
            }
        }else {
            downloadCurrentVideo();
        }
    }

    private void downloadCurrentVideo() {
        Utils.showLoading("Saving...");

        StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        File localFile = null;
        try {
            localFile = createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception: " + e.getMessage());
        }

        httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                 Utils.hideLoading();
                Log.d(TAG, "File Saved");
                Utils.showToast(VideoPlayerActivity.this, "Video Saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.hideLoading();
                Log.d(TAG, "Exception: " + e.getMessage());
                Utils.showToast(VideoPlayerActivity.this, "Error Occured: " + e.getMessage());
            }
        });
    }

    private File createNewFile() throws IOException {
        String videoFileName = "VID_" + System.currentTimeMillis();
        File videoFile = new File(Environment.getExternalStorageDirectory() + File.separator + "XMemo", videoFileName + ".mp4");
        videoFile.createNewFile();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoFile)));
        return videoFile;
    }

    //Share Video
    private void shareCurrentVideo() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "XMemo");
            intent.putExtra(Intent.EXTRA_TEXT, videoUrl);
            startActivity(Intent.createChooser(intent, "Share Video"));
        } catch(Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }
}