package com.app.xmemo.xmemo_image.app;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Khalid Khan on 23-06-2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
