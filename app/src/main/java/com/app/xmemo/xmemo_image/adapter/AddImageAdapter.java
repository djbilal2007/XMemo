package com.app.xmemo.xmemo_image.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.app.xmemo.xmemo_image.R;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Khalid Khan on 18,May,2017
 * Email khalid.khan@ratufa.com.
 */
public class AddImageAdapter extends BaseAdapter {

    private Context context;
    private List<Uri> imagesList;

    public AddImageAdapter(Context context, List<Uri> imagesList) {
        this.context = context;
        this.imagesList = imagesList;
    }

    @Override
    public int getCount() {
        return imagesList.size();
    }

    @Override
    public Uri getItem(int position) {
        return imagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


/*
        final ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
*/

        /*imageView.setImageURI(imagesList.get(position));
        ((BitmapDrawable)imageView.getDrawable()).getBitmap().recycle();*/

        View view = LayoutInflater.from(context).inflate(R.layout.image_details, parent, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        Glide.with(context)
                .load(imagesList.get(position))
                .thumbnail(0.5f)
                .into(imageView);

        return view;
    }
}
