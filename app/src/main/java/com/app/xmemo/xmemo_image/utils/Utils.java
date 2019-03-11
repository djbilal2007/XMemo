package com.app.xmemo.xmemo_image.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Khalid Khan on 11-06-2017.
 */

public class Utils {

    private static ProgressDialog progressDialog;
    private static MarshMallowPermission marshMallowPermission;


    public static Typeface setFont(Context context){
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/calibri.ttf");
        return typeface;
    }

    /*public static void onBackButtonPressed(Activity activity) {
        *//*back_btn = (ImageView)((Activity)currentActivity).findViewById(R.id.back_arrow_icon);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentActivity.startActivity(new Intent(currentActivity, nextActivity));
                ((Activity)currentActivity).finish();
            }
        });*//*
        activity.finish();
    }*/

    public static void showToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean isValidEmailAddress(String emailAddress) {
        String expression = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
        CharSequence inputStr = emailAddress;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password) {
        final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[A-Za-z])(?=.*[@#$%-_]).{6,20})";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean isInternetConnected(Context context){
        try {
            ConnectivityManager connect = null;
            connect = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connect != null) {
                NetworkInfo resultMobile = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                NetworkInfo resultWifi = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if ((resultMobile != null && resultMobile.isConnectedOrConnecting()) || (resultWifi != null && resultWifi.isConnectedOrConnecting())) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Utility", "Exception: " +  e.getMessage());
        }
        return false;
    }

    /*public static File createImageFile() throws IOException {
        String imageFileName = "IMG_" + System.currentTimeMillis();
        //File storageDirectory = Environment.getExternalStoragePublicDirectory("XMemo/camera");
        File storageDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "XMemo" + File.separator + "camera");
        if(!storageDirectory.exists()){
            storageDirectory.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        return image;
    }

    public static String getRealPathFromUri(Uri contentUri, Context context) {
        String [] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(context , contentUri, proj, null, null,null);
        Cursor cursor = cursorLoader.loadInBackground();
        int colmn_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(colmn_index);
        cursor.close();
        return result;
    }*/

    public static void initLoading(Context context){
        progressDialog = new ProgressDialog(context, R.style.MyTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress_style));
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);
    }

    public static void showLoading(String msg){
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void hideLoading(){
        progressDialog.dismiss();
    }

    public static void exitApp(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity)context).finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static boolean checkPermissionForReadAndCamera(Activity activity) {
        marshMallowPermission = new MarshMallowPermission(activity);

        if(!marshMallowPermission.checkPermissionForCamera()){
            marshMallowPermission.requestPermissionForCamera();
            return false;
        }else if(!marshMallowPermission.checkPermissionForReadStorage()){
            marshMallowPermission.requestPermissionForReadStorage();
            return false;
        }else if(!marshMallowPermission.checkPermissionForExternalStorage()){
            marshMallowPermission.requestPermissionForExternalStorage();
            return false;
        }else {
            return true;
        }
    }

    public static int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notify_tranparent : R.mipmap.app_icon;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
