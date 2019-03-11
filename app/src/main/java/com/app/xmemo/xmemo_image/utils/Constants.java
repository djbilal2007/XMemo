package com.app.xmemo.xmemo_image.utils;

import android.net.Uri;

import com.app.xmemo.xmemo_image.bean.Folder;
import com.app.xmemo.xmemo_image.bean.Image;
import com.app.xmemo.xmemo_image.bean.Video;
import com.onegravity.contactpicker.contact.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khalid Khan on 06,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class Constants {
    public static final String APP_NAME = "XMemo";
    public static final ArrayList<Uri> imageURIList = new ArrayList<>();
    public static final ArrayList<Uri> videoUrlList = new ArrayList<>();

    public static final ArrayList<Image> imageFetchedObjectList = new ArrayList<>();
    public static final ArrayList<String> imageFetchedList = new ArrayList<>();

    public static final ArrayList<String> videosFetchedList = new ArrayList<>();
    public static final ArrayList<String> videosThumbnailFetchedList = new ArrayList<>();
    public static final ArrayList<String> videosNameFetchedList = new ArrayList<>();
    public static final ArrayList<Video> videoFetchedObjectList  = new ArrayList<>();

    public static final List<Folder> imageFolderList  = new ArrayList<>();
    public static final List<Folder> videoFolderList  = new ArrayList<>();

    public static final List<Contact> contactList  = new ArrayList<>();


}
