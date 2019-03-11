package com.app.xmemo.xmemo_image.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.bean.SettingsOption;

import java.util.List;

/**
 * Created by Khalid Khan on 12,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class SettingsAdapter extends BaseAdapter {

    private Context context;
    private List<SettingsOption> settingsList;
    private LayoutInflater inflater;

    private ImageView settings_icon;
    private TextView settings_name;

    public SettingsAdapter(Context context, List<SettingsOption> settingsList) {
        this.context = context;
        this.settingsList = settingsList;
    }

    @Override
    public int getCount() {
        return settingsList.size();
    }

    @Override
    public SettingsOption getItem(int position) {
        return settingsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.settings_option_layout, parent, false);

        settings_icon = (ImageView)view.findViewById(R.id.settings_option_icon);
        settings_name = (TextView)view.findViewById(R.id.settings_option_name);

        settings_icon.setImageResource(settingsList.get(position).getImage());
        settings_name.setText(settingsList.get(position).getName());
        return view;
    }
}
