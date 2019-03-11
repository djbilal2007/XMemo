package com.app.xmemo.xmemo_image.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.activity.ChangePasswordActivity;
import com.app.xmemo.xmemo_image.activity.EditProfileActivity;
import com.app.xmemo.xmemo_image.activity.LoginActivity;
import com.app.xmemo.xmemo_image.activity.PrivacyPolicyActivity;
import com.app.xmemo.xmemo_image.adapter.SettingsAdapter;
import com.app.xmemo.xmemo_image.bean.SettingsOption;
import com.app.xmemo.xmemo_image.bean.User;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khalid Khan on 06,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class SettingsFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ImageView img_edit_profile, profile_pic;
    private TextView txt_fullname, txt_email;
    private ListView listView;
    private List<SettingsOption> settingsOptionList;
    private SettingsAdapter adapter;

    private static User user;
    private ProgressDialog pDialog;
    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_settings, container, false);

        setHasOptionsMenu(true);

        Typeface typeface = Utils.setFont(getContext());

        profile_pic = (ImageView) root.findViewById(R.id.profile_pic_settings);

        txt_fullname = (TextView)root.findViewById(R.id.txt_fullname_settings);
        txt_fullname.setTypeface(typeface);

        txt_email = (TextView)root.findViewById(R.id.txt_email_settings);
        txt_email.setTypeface(typeface);

        img_edit_profile = (ImageView) root.findViewById(R.id.img_edit_profile);
        img_edit_profile.setOnClickListener(this);

        settingsOptionList = new ArrayList<>();
        settingsOptionList.add(new SettingsOption(R.mipmap.change_password_icon, "Change Password"));
        settingsOptionList.add(new SettingsOption(R.mipmap.share_icon, "Share App"));
        settingsOptionList.add(new SettingsOption(R.mipmap.rate_icon, "Rate App"));
        settingsOptionList.add(new SettingsOption(R.mipmap.policy_icon, "Privacy Policy"));
        settingsOptionList.add(new SettingsOption(R.mipmap.logout_icon, "Logout"));

        adapter = new SettingsAdapter(getContext(), settingsOptionList);
        listView = (ListView)root.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserDetails();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }


    private void getUserDetails() {
        showProgressDialog();
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("userList/" + userUID);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                txt_fullname.setText(user.getName());
                txt_email.setText(user.getEmail());
                Glide.with(mActivity)
                        .load(user.getProfileImage())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                Utils.showToast(getContext(), "Exception Occured");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                if (mActivity.isDestroyed()) {
                                    return false;
                                }
                                dismissProgressDialog();
                                return false;
                            }
                        })
                        .thumbnail(0.5f)
                        .crossFade()
                        .error(R.mipmap.default_user_img)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profile_pic);

                dismissProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("SettingsFragment", "Exception: " + databaseError.getMessage());

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == img_edit_profile){
            openEditProfileActivity();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                openChangePasswordActivity();
                break;
            case 1:
                openShareApp();
                break;
            case 2:
                openRateApp();
                break;
            case 3:
                openPrivacyPolicy();
                break;
            case 4:
                logout();
                break;
        }
    }

    private void openEditProfileActivity() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        intent.putExtra("name", user.getName());
        intent.putExtra("email", user.getEmail());
        intent.putExtra("photoUrl", user.getProfileImage());
        startActivity(intent);
    }

    private void openChangePasswordActivity() {
        startActivity(new Intent(getContext(), ChangePasswordActivity.class));
        //getActivity().finish();
    }

    private void openShareApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "XMemo");
            String str = "www.google.com";
            intent.putExtra(Intent.EXTRA_TEXT, str);
            startActivity(Intent.createChooser(intent, "Share XMemo"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    private void openRateApp() {
    }

    private void openPrivacyPolicy() {
        startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .show();
    }

    @Override
    public void onDestroy() {
     //   Utils.hideLoading();
        dismissProgressDialog();
        super.onDestroy();
    }


    private void showProgressDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(getActivity(), R.style.MyTheme);
            pDialog.setMessage("Loading....");
            pDialog.setIndeterminate(false);
            pDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_style));
            pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pDialog.setCancelable(false);
        }
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}
