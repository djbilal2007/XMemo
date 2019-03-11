package com.app.xmemo.xmemo_image.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.bean.Contact;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/**
 * Created by Khalid Khan on 19,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class ContactSaveToPhoneAdapter extends RecyclerView.Adapter<ContactSaveToPhoneAdapter.MyViewHolder> {

    private Context context;
    private List<Contact> contactList;

    public ContactSaveToPhoneAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_item_save, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Contact contact = contactList.get(position);

        holder.checkBox.setChecked(contact.isSelected());
        holder.checkBox.setTag(contact);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Contact c = (Contact) cb.getTag();
                c.setSelected(cb.isChecked());
                contact.setSelected(cb.isChecked());
            }
        });

        if(contact.getImageUrl() != null){
        Glide.with(context)
                .load(contact.getImageUrl())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);
        }else {
            holder.imageView.setImageResource(R.mipmap.default_user_img);
        }

        if(contact.getFullName() != null){
            holder.txt_name.setText(contact.getFullName());
        }

        if(contact.getEmail() != null){
            holder.txt_email.setVisibility(View.VISIBLE);
            holder.txt_email.setText(contact.getEmail());
        }else {
            holder.txt_email.setVisibility(View.GONE);
        }

        if(contact.getPhoneNumber() != null){
            holder.txt_phone.setVisibility(View.VISIBLE);
            holder.txt_phone.setText(contact.getPhoneNumber());
        }else {
            holder.txt_phone.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
            return contactList.size();
            }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        private TextView txt_name, txt_email, txt_phone;
        private CheckBox checkBox;

        public MyViewHolder(View view) {
            super(view);

            imageView = (ImageView)view.findViewById(R.id.img_contact);
            txt_name = (TextView)view.findViewById(R.id.txt_name);
            txt_email = (TextView)view.findViewById(R.id.txt_email);
            txt_phone = (TextView)view.findViewById(R.id.txt_phone);
            checkBox = (CheckBox)view.findViewById(R.id.check_contact_select);
        }
    }

    public List<Contact> getStudentList() {
        return contactList;
    }
}
