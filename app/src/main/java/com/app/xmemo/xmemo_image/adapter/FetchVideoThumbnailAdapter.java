package com.app.xmemo.xmemo_image.adapter;

/**
 * Created by Khalid Khan on 20,June,2017
 * Email khalid.khan@ratufa.com.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.activity.VideoPlayerActivity;
import com.app.xmemo.xmemo_image.bean.Video;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/**
 * Created by Khalid Khan on 07,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class FetchVideoThumbnailAdapter extends BaseAdapter{

    private Context context;
//    private List<String> imagesList;
    private List<Video> videoObjectList;
    private String folder_name, folder_key;

/*
    public FetchVideoThumbnailAdapter(Context context, List<String> imagesList, String folder_name) {
        this.context = context;
        this.imagesList = imagesList;
        this.folder_name = folder_name;
    }
*/

    public FetchVideoThumbnailAdapter(Context context, List<Video> videoObjectList, String folder_name, String folder_key) {
        this.context = context;
        this.videoObjectList = videoObjectList;
        this.folder_name = folder_name;
        this.folder_key = folder_key;
    }

    @Override
    public int getCount() {
//        return imagesList.size();
        return videoObjectList.size();

    }

    @Override
    public Video getItem(int position) {
        //return imagesList.get(position);
        return videoObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.thumbnail_view, parent, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.img_video_thubmnail);
        ImageButton btn_play = (ImageButton) view.findViewById(R.id.btn_play_video);

        Glide.with(context)
                //.load(imagesList.get(position))
                .load(videoObjectList.get(position).getImageUrl())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Video video = videoObjectList.get(position);
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("thumbnailUrl", video.getImageUrl());
                intent.putExtra("videoUrl", video.getFileUrl());
                intent.putExtra("videoName", video.getName());
                intent.putExtra("folder_name", folder_name);
                intent.putExtra("folder_key", folder_key);
                context.startActivity(intent);

                /*Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("thumbnailUrl", Constants.videosThumbnailFetchedList.get(position));
                intent.putExtra("videoUrl", Constants.videosFetchedList.get(position));
                intent.putExtra("videoName", Constants.videosNameFetchedList.get(position));
                intent.putExtra("folder_name", folder_name);
                context.startActivity(intent);*/
            }
        });

        return view;

      /*  ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(300, 300));

        Glide.with(context)
                .load(imagesList.get(position))
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        return imageView;*/
    }
}
