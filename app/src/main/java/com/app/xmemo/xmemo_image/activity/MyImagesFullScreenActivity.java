package com.app.xmemo.xmemo_image.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.adapter.FullScreenImageAdapter;
import com.app.xmemo.xmemo_image.bean.Image;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;
import com.app.xmemo.xmemo_image.utils.Constants;
import com.app.xmemo.xmemo_image.utils.ExtendedViewPager;
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyImagesFullScreenActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    //private ViewPager viewPager;
    private ExtendedViewPager viewPager;
    private FullScreenImageAdapter adapter;
    private ImageButton share_imgBtn, download_imgBtn, delete_imgBtn, back_arrow;
    public static TextView image_count, total_image_count;
    private static String folder_key = "", userUID = "";


    private MarshMallowPermission marshMallowPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_my_images_full_screen);

        Utils.initLoading(this);

        marshMallowPermission = new MarshMallowPermission(this);

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        share_imgBtn = (ImageButton)findViewById(R.id.img_shareBtn);
        share_imgBtn.setOnClickListener(this);

        download_imgBtn = (ImageButton)findViewById(R.id.img_downloadBtn);
        download_imgBtn.setOnClickListener(this);

        delete_imgBtn = (ImageButton)findViewById(R.id.img_deleteBtn);
        delete_imgBtn.setOnClickListener(this);

        back_arrow = (ImageButton)findViewById(R.id.img_backArrowImage);
        back_arrow.setOnClickListener(this);

        image_count = (TextView)findViewById(R.id.image_count);
        total_image_count = (TextView)findViewById(R.id.total_image_count);

        viewPager = (ExtendedViewPager)findViewById(R.id.viewPager_fullScreenImages);
        viewPager.addOnPageChangeListener(this);

        int pos = getIntent().getIntExtra("pos", 0);

        folder_key = getIntent().getStringExtra("folder_key");

        adapter = new FullScreenImageAdapter(this, Constants.imageFetchedList);
        viewPager.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        viewPager.setCurrentItem(pos);

        image_count.setText("" + (pos+1));
        total_image_count.setText("" + Constants.imageFetchedList.size());
    }

    @Override
    public void onClick(View v) {
        if(v == share_imgBtn){
            shareCurrentImage();
        }
        if(v == download_imgBtn){
            //downloadCurrentImage();
            checkPermissionForStorage();
        }
        if(v == delete_imgBtn){
            createAlertDialog();
        }
        if(v == back_arrow){
            onBackPressed();
        }
    }

    //Delete Image
    int pos;

    private void createAlertDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setMessage("File will be permanently deleted. Are you sure?");
        build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCurrentImage();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        build.show();
    }

    private void deleteCurrentImage() {
        Utils.showLoading("Deleting File.....");
        pos = viewPager.getCurrentItem();

        final String fileName = Constants.imageFetchedObjectList.get(pos).getName();
        Constants.imageFetchedObjectList.remove(pos);
        adapter.notifyDataSetChanged();
        Log.d("FullScreenActivity", "Filename: " + fileName);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference reference = storageReference.child(userUID + "/uploadedImages/" + folder_key + "/" + fileName);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FullScreenActivity", "File Deleted Succesfully");
                deleteFromDatabase(fileName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.hideLoading();
                Log.d("FullScreenActivity", "Exception: " + e.getMessage());
            }
        });
    }

    private void deleteFromDatabase(final String fileName) {
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedImages/" + folder_key + "/imagesData");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Image image = ds.getValue(Image.class);
                    if(image.getName().equals(fileName)){
                        Log.d("FullScreenActivity", "File Found");
                        Log.d("FullScreenActivity", "Key: " + ds.getKey());

                        databaseRef.child(ds.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("FullScreenActivity", "Data deleted from Database");
                                adapter.notifyDataSetChanged();
                                if (pos == Constants.imageFetchedList.size()){
                                    pos--;
                                    viewPager.setCurrentItem(pos);
                                    image_count.setText("" + (pos+1));
                                    total_image_count.setText(Constants.imageFetchedList.size());
                                }else{
                                    viewPager.setCurrentItem(pos);
                                    image_count.setText("" + (pos+1));
                                    total_image_count.setText(Constants.imageFetchedList.size());
                                }
                                Utils.hideLoading();
                                Utils.showToast(MyImagesFullScreenActivity.this, "File Deleted Successfully");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("FullScreenActivity", "Data deletion failed. Exception: " + e.getMessage());
                                Utils.hideLoading();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FullScreenActivity", "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
            }
        });
    }

    //Download Image
    private void checkPermissionForStorage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!marshMallowPermission.checkPermissionForExternalStorage()) {
                marshMallowPermission.requestPermissionForExternalStorage();
            } else {
                downloadCurrentImage();
            }
        }else {
            downloadCurrentImage();
        }
    }

    private void downloadCurrentImage() {
        Utils.showLoading("Saving...");
        String imgToDownload = Constants.imageFetchedList.get(viewPager.getCurrentItem());

        StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(imgToDownload);

        File localFile = null;
        try {
            localFile = createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("FullScreenActivity", "Exception: " + e.getMessage());
        }

        httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Utils.hideLoading();
                Log.d("FullScreenActivity", "Image Saved");
                Utils.showToast(MyImagesFullScreenActivity.this, "File Saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.hideLoading();
                Log.d("FullScreenActivity", "Exception: " + e.getMessage());
            }
        });
    }

    private File createNewFile() throws IOException {
        String imageFileName = "IMG_" + System.currentTimeMillis();
        File image = new File(Environment.getExternalStorageDirectory() + File.separator + "XMemo", imageFileName + ".jpg");
        image.createNewFile();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image )));
        return image;
    }

    Bitmap loadedImage = null;
    //Share Image
    private void shareCurrentImage() {
        Utils.showLoading("Preparing to share....");
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException e) {
                    Log.d("FullScreenActivity", "Exception: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                loadedImage = bitmap;

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "");
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), loadedImage, "", null);
                Uri screenshotUri = Uri.parse(path);

                intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                intent.setType("image/*");
                startActivity(Intent.createChooser(intent, "Share image via..."));
                Utils.hideLoading();
            }
        }.execute(Constants.imageFetchedList.get(viewPager.getCurrentItem()));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        image_count.setText("" + (position+1));
        total_image_count.setText("" + Constants.imageFetchedList.size());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
