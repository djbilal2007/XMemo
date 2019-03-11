package com.app.xmemo.xmemo_image.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.xmemo.xmemo_image.R;
import com.onegravity.contactpicker.contact.Contact;

import java.util.List;

/**
 * Created by Khalid Khan on 18-06-2017.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder>  {

    private Context context;
    private List<Contact> contactList;

    public ContactsAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        if(contact.getPhotoUri() != null){
            holder.imageView.setImageURI(contact.getPhotoUri());
        }else {
            holder.imageView.setImageResource(R.mipmap.default_user_img);
        }


        holder.txt_name.setText(contact.getDisplayName());

        if(contact.getEmail(0) != null){
            holder.txt_email.setVisibility(View.VISIBLE);
            holder.txt_email.setText(contact.getEmail(0));
        }else {
            holder.txt_email.setVisibility(View.GONE);
        }

        if(contact.getPhone(0) != null){
            holder.txt_phone.setVisibility(View.VISIBLE);
            holder.txt_phone.setText(contact.getPhone(0));
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

        public MyViewHolder(View view) {
            super(view);

            imageView = (ImageView)view.findViewById(R.id.img_contact);
            txt_name = (TextView)view.findViewById(R.id.txt_name);
            txt_email = (TextView)view.findViewById(R.id.txt_email);
            txt_phone = (TextView)view.findViewById(R.id.txt_phone);
        }
    }
}
