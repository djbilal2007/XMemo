package com.app.xmemo.xmemo_image.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.bean.Folder;
import com.app.xmemo.xmemo_image.bean.Image;
import com.app.xmemo.xmemo_image.bean.Video;
import com.app.xmemo.xmemo_image.fragment.ImageDetailsFragment;
import com.app.xmemo.xmemo_image.fragment.VideoDetailsFragment;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Khalid Khan on 08,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class    FolderImageAdapter extends BaseAdapter {

    private Context context;
    private List<Folder> folderList;
    private LayoutInflater inflater;
    private String media;

    private ImageView folder_imageView, three_dots;
    private TextView folder_name_txt, folder_file_count;
    private CardView cardView;

    private static String userUID = "";

    public FolderImageAdapter(Context context, List<Folder> folderList, String media) {
        this.context = context;
        this.folderList = folderList;
        this.media = media;

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        Utils.initLoading(context);
    }

    @Override
    public int getCount() {
        return folderList.size();
    }

    @Override
    public Folder getItem(int position) {
        return folderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.folder_layout, parent, false);

        cardView = (CardView)view.findViewById(R.id.card_view);
        folder_imageView = (ImageView)view.findViewById(R.id.folder_image);
        folder_name_txt = (TextView) view.findViewById(R.id.folder_name_txt);
        folder_file_count = (TextView) view.findViewById(R.id.folder_file_count);

        three_dots= (ImageView)view.findViewById(R.id.three_dots_option);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderKey = folderList.get(position).getKey();
                String folderName = folderList.get(position).getName();
                openFolderDetailFragment(folderKey, folderName);
            }
        });

        final Folder folder = folderList.get(position);
        folder_name_txt.setText(folder.getName());

        if(folder.getFileCount() > 1 && media.equals("images")){
            folder_file_count.setText(folder.getFileCount() + " Photos");
        }else if(folder.getFileCount() == 1 && media.equals("images")){
            folder_file_count.setText(folder.getFileCount() + " Photo");
        }else if(folder.getFileCount() > 1 && media.equals("videos")){
            folder_file_count.setText(folder.getFileCount() + " Videos");
        }else if(folder.getFileCount() == 1 && media.equals("videos")){
            folder_file_count.setText(folder.getFileCount() + " Video");
        }

        Glide.with(context)
                .load(folder.getImageUrl())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(folder_imageView);

        three_dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Log.d("FolderImage", "Rename " + folder.getName());
                            renameFolder(folder.getName(), folder.getKey());
                        }else if(which == 1){
                            Log.d("FolderImage", "Delete " + folder.getName());
                            deleteFolder(folder.getName(), folder.getKey());
                        }
                    }
                });
                builder.show();
            }
        });

        return view;
    }

    //Delete Folder
    private void deleteFolder(String name, String key) {
        createAlertDialog(name, key);
    }

    private void createAlertDialog(final String folderName, final String key) {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setMessage("Folder will be permanently deleted. Are you sure?");
        build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(media.equals("images")){
                    deleteImageFolderFromStorage(folderName, key);
                }else if(media.equals("videos")){
                    deleteVideoFolderFromStorage(folderName, key);
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        build.show();
    }

    private void deleteImageFolderFromStorage(final String folderName, final String folderKey) {
        Utils.showLoading("Deleting Folder.....");
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedImages/" + folderKey);
        databaseRef.child("imagesData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Image image = ds.getValue(Image.class);
                    Log.d("FolderImage", "Name: " + image.getName());
                    String fileName = image.getName();

                    StorageReference reference = FirebaseStorage.getInstance().getReference().child(userUID + "/uploadedImages/" + folderName + "/" + fileName);
                    reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FolderImage", "File Deleted Succesfully");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FolderImage", "Exception: " + e.getMessage());
                            Utils.hideLoading();
                        }
                    });
                }
                deleteImageFolderFromDatabase(folderKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FolderImage", "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
            }
        });
    }

    private void deleteImageFolderFromDatabase(String folderKey) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedImages/" + folderKey);
        databaseRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FolderImage", "Folder Deleted");
                Utils.hideLoading();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FolderImage", "Exception : " + e.getMessage());
                Utils.hideLoading();
                Utils.showToast(context, "Exception Occured");
            }
        });
    }

    private void deleteVideoFolderFromStorage(final String folderName, final String folderKey) {
        Utils.showLoading("Deleting Folder.....");
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedVideos/" + folderKey);
        databaseRef.child("videoData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Video video = ds.getValue(Video.class);
                    Log.d("FolderImage", "Name: " + video.getName());
                    final String fileName = video.getName();

                    StorageReference reference = FirebaseStorage.getInstance().getReference().child(userUID + "/uploadedVideos/" + folderName + "/" + fileName);
                    reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FolderImage", "File Deleted Succesfully");
                            deleteVideoThumbnailFromStorage(folderName, fileName);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FolderImage", "Exception: " + e.getMessage());
                            Utils.hideLoading();
                        }
                    });
                }
                deleteVideoFolderFromDatabase(folderKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FolderImage", "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
            }
        });
    }

    private void deleteVideoThumbnailFromStorage(String folderName, String fileName) {
        String subfileName = fileName.split("_")[1];
        String fullFileName = "IMG_" + subfileName;

        StorageReference reference = FirebaseStorage.getInstance().getReference().child(userUID + "/uploadedVideos/" + folderName + "/thumbnails/" + fullFileName);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FolderImage", "Thumbnail Deleted Succesfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FolderImage", "Exception: " + e.getMessage());
                Utils.hideLoading();
                Utils.showToast(context, "Exception Occured");
            }
        });
    }

    private void deleteVideoFolderFromDatabase(String folderKey) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedVideos/" + folderKey);
        databaseRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FolderImage", "Video Folder Deleted");
                Utils.hideLoading();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FolderImage", "Exception : " + e.getMessage());
                Utils.hideLoading();
                Utils.showToast(context, "Exception Occured");
            }
        });
    }
    ////////////

    //Rename Folder
    private void renameFolder(String folderName, final String folderKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename Folder");
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        editText.setText(folderName);
        builder.setView(editText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.showLoading("Renaming Folder.....");
                String updatedFolderName = editText.getText().toString().trim();

                if(media.equals("images")){
                    renameImageFolderInFirebase(updatedFolderName, folderKey);
                }else if(media.equals("videos")){
                    renameVideoFolderInFirebase(updatedFolderName, folderKey);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void renameImageFolderInFirebase(final String folder_name, String folder_key) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedImages/" + folder_key);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("folderName", folder_name);
                databaseReference.updateChildren(childUpdates);
                Utils.hideLoading();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FolderImage", "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
                Utils.showToast(context, "Exception: " + databaseError.getMessage());
            }
        });
    }

    private void renameVideoFolderInFirebase(final String folder_name, String folder_key) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedVideos/" + folder_key);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("folderName", folder_name);
                databaseReference.updateChildren(childUpdates);
                Utils.hideLoading();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FolderImage", "Exception: " + databaseError.getMessage());
                Utils.hideLoading();
                Utils.showToast(context, "Exception: " + databaseError.getMessage());
            }
        });
    }

    /////////


    private void openFolderDetailFragment(String folderKey, String folderName) {

        if(media.equals("images")){
            Fragment fragment = new ImageDetailsFragment();
            Bundle args = new Bundle();
            args.putString("folderKey", folderKey);
            args.putString("folderName", folderName);
            fragment.setArguments(args);

            FragmentTransaction transaction = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack("image_details");
            transaction.commit();
        }else if(media.equals("videos")){
            Fragment fragment = new VideoDetailsFragment();
            Bundle args = new Bundle();
            args.putString("folderKey", folderKey);
            args.putString("folderName", folderName);
            fragment.setArguments(args);

            FragmentTransaction transaction = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack("video_details");
            transaction.commit();
        }
    }
}
