package com.app.xmemo.xmemo_image.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.utils.TouchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/**
 * Created by Khalid Khan on 19,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class FullScreenImageAdapter extends PagerAdapter {

    public TouchImageView imageView;
    private Context context;
    private List<String> imgUrlList;
    private LayoutInflater inflater;
    private RelativeLayout relativeLayout;

    public FullScreenImageAdapter(Context context, List<String> imgUrlList) {
        this.context = context;
        this.imgUrlList = imgUrlList;
    }

    @Override
    public int getCount() {
        return imgUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_fullscreen_image, container, false);
        imageView = (TouchImageView)view.findViewById(R.id.imgDisplay);
        relativeLayout = (RelativeLayout)view.findViewById(R.id.relative_layout);

        Glide.with(context)
                .load(imgUrlList.get(position))
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

}
