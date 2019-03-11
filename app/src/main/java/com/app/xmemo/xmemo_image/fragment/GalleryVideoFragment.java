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
import com.app.xmemo.xmemo_image.activity.AddVideoActivity;
import com.app.xmemo.xmemo_image.adapter.FolderImageAdapter;
import com.app.xmemo.xmemo_image.bean.Folder;
import com.app.xmemo.xmemo_image.bean.Video;
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
public class GalleryVideoFragment extends Fragment implements View.OnClickListener {

    private GridView gridView;
    private ImageButton addVidBtn;

    private FolderImageAdapter adapter;

    private static String userUID = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery_videos, container, false);

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        gridView = (GridView) root.findViewById(R.id.gridView_videos);

        addVidBtn = (ImageButton) root.findViewById(R.id.add_vidBtn);
        addVidBtn.setOnClickListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getVideosFromServer();
    }

    private void getVideosFromServer() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedVideos");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.videoFolderList.clear();
                Log.d("GalleryVideoFragment", "" + dataSnapshot.getChildrenCount());

                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String folder_name = ds.child("folderName").getValue().toString();
                    DataSnapshot d1 = ds.child("videoData");

                    Log.d("GalleryVideoFragment", "Key: " + ds.getKey());
                    Log.d("GalleryVideoFragment", "Folder Name: " + folder_name);
                    Log.d("GalleryVideoFragment", "File Count: " + d1.getChildrenCount());

                    String folder_key = ds.getKey();

                    String folderImageUrl = "", time = "";
                    long fileCount = d1.getChildrenCount();
                    for(DataSnapshot d : d1.getChildren()){
                        Video video = d.getValue(Video.class);
                        folderImageUrl = video.getImageUrl();
                        time = video.getTime();
                        break;
                    }

                    Folder folder = new Folder(folder_name, folderImageUrl, time, fileCount, folder_key);
                    Constants.videoFolderList.add(folder);
                }

                adapter = new FolderImageAdapter(getActivity(), Constants.videoFolderList, "videos");
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("GalleryVideoFragment", "" + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Constants.videosFetchedList.clear();
    }

    @Override
    public void onClick(View v) {
        if (v == addVidBtn) {
            if (Utils.checkPermissionForReadAndCamera(getActivity())) {
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
                Log.d("GalleryVideo", "Folder Name: " + folder_name);
                goToAddVideoActivity(folder_name);
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private void goToAddVideoActivity(String folder_name) {
        Intent intent = new Intent(getActivity(), AddVideoActivity.class);
        intent.putExtra("folder_name", folder_name);
        startActivity(intent);
    }
}