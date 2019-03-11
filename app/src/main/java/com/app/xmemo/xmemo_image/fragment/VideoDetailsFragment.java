package com.app.xmemo.xmemo_image.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.activity.AddVideoActivity;
import com.app.xmemo.xmemo_image.adapter.FetchVideoThumbnailAdapter;
import com.app.xmemo.xmemo_image.bean.Video;
import com.app.xmemo.xmemo_image.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by Khalid Khan on 09,June,2017
 * Email khalid.khan@ratufa.com.
 */

public class VideoDetailsFragment extends Fragment implements View.OnClickListener {

    private GridView gridView;
    private FetchVideoThumbnailAdapter adapter;

    private ImageView back_arrow;
    private TextView header_txt;
    private ImageButton add_btn;

    private static String folder_name = "", folder_key = "";
    private static String userUID = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery_video_details, container, false);

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        folder_key = getArguments().getString("folderKey");
        folder_name = getArguments().getString("folderName");

        header_txt = (TextView)root.findViewById(R.id.header_txt_video_frag);
        header_txt.setText(folder_name);

        back_arrow = (ImageView)root.findViewById(R.id.back_photo_vid);
        back_arrow.setOnClickListener(this);

        add_btn = (ImageButton)root.findViewById(R.id.add_vidBtn_folder);
        add_btn.setOnClickListener(this);

        gridView = (GridView)root.findViewById(R.id.gridView_folder_video_details);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getVideosFromServer();
    }

    private void getVideosFromServer() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedVideos/" + folder_key);
        databaseRef.child("videoData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*Constants.videosThumbnailFetchedList.clear();
                Constants.videosFetchedList.clear();
                Constants.videosNameFetchedList.clear();*/

                Constants.videoFetchedObjectList.clear();
                Log.d("VideoDetailsFragment", "" + dataSnapshot.getChildrenCount());
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Video video = ds.getValue(Video.class);

                    /*Constants.videosThumbnailFetchedList.add(video.getImageUrl());
                    Constants.videosFetchedList.add(video.getFileUrl());
                    Constants.videosNameFetchedList.add(video.getName());*/
                    Constants.videoFetchedObjectList.add(video);
                }

//                adapter = new FetchVideoThumbnailAdapter(getActivity(), Constants.videosThumbnailFetchedList, folder_name);
                adapter = new FetchVideoThumbnailAdapter(getActivity(), Constants.videoFetchedObjectList, folder_name, folder_key);
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("VideoDetailsFragment", "DatabaseError: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onClick(View v) {
        if(v == back_arrow){
            openGalleryVideoFragment();
        }
        if(v == add_btn){
            goToAddVideoActivity();
        }
    }

    private void goToAddVideoActivity() {
        Intent intent = new Intent(getActivity(), AddVideoActivity.class);
        intent.putExtra("folder_key", folder_key);
        intent.putExtra("folder_name", folder_name);
        startActivity(intent);
    }

    private void openGalleryVideoFragment() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Constants.videosFetchedList.clear();
        Constants.videosThumbnailFetchedList.clear();
    }
}
