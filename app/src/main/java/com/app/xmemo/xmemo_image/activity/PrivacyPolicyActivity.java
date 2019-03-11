package com.app.xmemo.xmemo_image.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.app.xmemo.xmemo_image.R;

public class PrivacyPolicyActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView back_arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        back_arrow = (ImageView)findViewById(R.id.back_arrow_privacy_policy);
        back_arrow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == back_arrow) {
            onBackPressed();
        }
    }
}
