package com.app.xmemo.xmemo_image.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.activity.AddImageActivity;
import com.app.xmemo.xmemo_image.adapter.FolderImageAdapter;
import com.app.xmemo.xmemo_image.bean.Folder;
import com.app.xmemo.xmemo_image.bean.Image;
import com.app.xmemo.xmemo_image.utils.Constants;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Khalid Khan on 06,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class GalleryPhotoFragment extends Fragment implements View.OnClickListener {

    private GridView gridView;
    private FolderImageAdapter adapter;

    private ImageButton addImgBtn;

    private static String userUID = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery_photo, container, false);

        Utils.initLoading(getActivity());

        gridView = (GridView)root.findViewById(R.id.gridView_folder);

        addImgBtn = (ImageButton) root.findViewById(R.id.add_imgBtn);
        addImgBtn.setOnClickListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        checkConnectivity();
    }

    private void checkConnectivity() {
        Utils.showLoading("Fetching Data, Please wait...");
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if(connected){
                    Log.d("GalleryPhotoFragment", "Connected");
                    getImagesFromServer();
                }else {
                    getImagesFromServer();
                    Log.d("GalleryPhotoFragment", "Not Connected");
                    //Utils.showToast(getActivity(), "Please check Internet Connectivity");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.hideLoading();
                Log.d("GalleryPhotoFragment", "Exception: " + databaseError.getMessage());
                Utils.showToast(getActivity(), "Please check Internet Connectivity");
            }
        });
    }

    private void getImagesFromServer() {
        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedImages");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.imageFolderList.clear();

                Log.d("GalleryPhotoFragment", "Folder Count: " + dataSnapshot.getChildrenCount());

                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String folder_name = ds.child("folderName").getValue().toString();
                    DataSnapshot d1 = ds.child("imagesData");

                    Log.d("GalleryPhotoFragment", "Key: " + ds.getKey());
                    Log.d("GalleryPhotoFragment", "Folder Name: " + folder_name);
                    Log.d("GalleryPhotoFragment", "File Count: " + d1.getChildrenCount());

                    String folder_key = ds.getKey();

                    String folderImageUrl = "", time = "";
                    long fileCount = d1.getChildrenCount();
                    for(DataSnapshot d : d1.getChildren()){
                        Image image = d.getValue(Image.class);
                        folderImageUrl = image.getImageUrl();
                        time = image.getTime();
                        break;
                    }

/*

                    String folderImageUrl = "", time = "";
                    long fileCount = 0;
                    Log.d("GalleryPhotoFragment", ds.getKey());
                    for (DataSnapshot d : ds.getChildren()) {
                        fileCount = ds.getChildrenCount();
                        Image image = d.getValue(Image.class);
                        folderImageUrl = image.getImageUrl();
                        time = image.getTime();
                        break;
                    }
*/
                    Folder folder = new Folder(folder_name, folderImageUrl, time, fileCount, folder_key);
                    Constants.imageFolderList.add(folder);
                }


                adapter = new FolderImageAdapter(getActivity(), Constants.imageFolderList, "images");
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                Utils.hideLoading();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.hideLoading();
                Log.d("GalleryPhotoFragment", "" + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Constants.imageFolderList.clear();
    }

    @Override
    public void onClick(View v) {
        if (v == addImgBtn) {
            if(Utils.checkPermissionForReadAndCamera(getActivity())){
                 createFolder();
            }
        }
    }

    private void createFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_folder_name, null);
        builder.setView(view);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folder_name = ((EditText) view.findViewById(R.id.edit_txt_folder_name)).getText().toString().trim();
                Log.d("GalleryFragment", "Folder Name: " + folder_name);
                goToAddImageActivity(folder_name);
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private void goToAddImageActivity(String folder_name) {
        Intent intent = new Intent(getActivity(), AddImageActivity.class);
        intent.putExtra("folder_name", folder_name);
        startActivity(intent);
    }
}
