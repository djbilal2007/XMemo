package com.app.xmemo.xmemo_image.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.activity.AddImageActivity;
import com.app.xmemo.xmemo_image.activity.MyImagesFullScreenActivity;
import com.app.xmemo.xmemo_image.adapter.FetchImageAdapter;
import com.app.xmemo.xmemo_image.bean.Image;
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

public class ImageDetailsFragment extends Fragment implements View.OnClickListener {

    private GridView gridView;
    private FetchImageAdapter adapter;

    private ImageView back_arrow;
    private TextView header_txt;
    private ImageButton add_btn;

    private static String folder_name = "", folder_key = "";
    private static String userUID = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery_photo_details, container, false);

        folder_key = getArguments().getString("folderKey");
        folder_name = getArguments().getString("folderName");

        header_txt = (TextView)root.findViewById(R.id.header_txt_photo_frag);
        header_txt.setText(folder_name);

        back_arrow = (ImageView)root.findViewById(R.id.back_photo_img);
        back_arrow.setOnClickListener(this);

        add_btn = (ImageButton)root.findViewById(R.id.add_imgBtn_folder);
        add_btn.setOnClickListener(this);

        gridView = (GridView)root.findViewById(R.id.gridView_folder_image_details);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getImagesFromServer();
    }

    private void getImagesFromServer() {
        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedImages/" + folder_key);
        databaseRef.child("imagesData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.imageFetchedList.clear();
                Constants.imageFetchedObjectList.clear();
                Log.d("ImageDetailsFragment", "" + dataSnapshot.getChildrenCount());
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Image image = ds.getValue(Image.class);
                    Constants.imageFetchedList.add(image.getImageUrl());
                    Constants.imageFetchedObjectList.add(image);
                }
                adapter = new FetchImageAdapter(getActivity(), Constants.imageFetchedList);
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ImageDetailsFragment", "DatabaseError: " + databaseError.getMessage());
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MyImagesFullScreenActivity.class);
                intent.putExtra("pos", position);
                intent.putExtra("folder_key", folder_key);
                intent.putExtra("folder_name", folder_name);
                startActivity(intent);
            }
        });

    }


    @Override
    public void onClick(View v) {
        if(v == back_arrow){
            openGalleryPhotoFragment();
        }
        if(v == add_btn){
            goToAddImageActivity();
        }
    }

    private void goToAddImageActivity() {
        Intent intent = new Intent(getActivity(), AddImageActivity.class);
        intent.putExtra("folder_key", folder_key);
        intent.putExtra("folder_name", folder_name);
        startActivity(intent);
    }

    private void openGalleryPhotoFragment() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Constants.imageFetchedList.clear();
    }
}
