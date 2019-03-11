package com.app.xmemo.xmemo_image.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.app.xmemo.xmemo_image.fragment.GalleryPhotoFragment;
import com.app.xmemo.xmemo_image.fragment.GalleryVideoFragment;


/**
 * Created by Khalid Khan on 06,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    int tabCount;

    public TabsPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new GalleryPhotoFragment();
            case 1:
                return new GalleryVideoFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
