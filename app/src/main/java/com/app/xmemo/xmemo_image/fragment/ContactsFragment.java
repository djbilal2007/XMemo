package com.app.xmemo.xmemo_image.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.activity.AddContactActivity;
import com.app.xmemo.xmemo_image.activity.AddContactToPhoneActivity;
import com.app.xmemo.xmemo_image.activity.DeleteContactActivity;
import com.app.xmemo.xmemo_image.adapter.ContactsFetchAdapter;
import com.app.xmemo.xmemo_image.bean.Contact;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Khalid Khan on 06,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class ContactsFragment extends Fragment implements View.OnClickListener, ContactsFetchAdapter.MyClickListener {

    private ImageButton add_contactBtn;

    private List<Contact> contactList;
    private RecyclerView recyclerView;
    private ContactsFetchAdapter adapter;
    private static String userUID = "";
    private ImageButton btn_saveToPhone, btn_deleteContact;

    private MarshMallowPermission marshMallowPermission;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);

        marshMallowPermission = new MarshMallowPermission(getActivity());

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        contactList = new ArrayList<>();

        add_contactBtn = (ImageButton) root.findViewById(R.id.add_contactBtn);
        add_contactBtn.setOnClickListener(this);

        btn_saveToPhone = (ImageButton) root.findViewById(R.id.img_saveToPhoneBtn);
        btn_saveToPhone.setOnClickListener(this);

        btn_deleteContact = (ImageButton) root.findViewById(R.id.img_deleteContactBtn);
        btn_deleteContact.setOnClickListener(this);

        recyclerView = (RecyclerView)root.findViewById(R.id.recyclerView_fetchContact);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getContactsFromServer();
    }

    private void getContactsFromServer() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedContacts");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contactList.clear();
                Log.d("ContactsFragment", "" + dataSnapshot.getChildrenCount());
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Contact contact = ds.getValue(Contact.class);
                    contactList.add(contact);
                }
                contactList = sortList(contactList);
                adapter = new ContactsFetchAdapter(getActivity(), contactList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                adapter.setOnItemClickListener(ContactsFragment.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ContactsFragment", "Exception: " + databaseError.getMessage());
            }
        });
    }

    private List<Contact> sortList(List<Contact> contactList) {
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.getFullName().compareToIgnoreCase(o2.getFullName());
            }
        });
        return contactList;
    }

    @Override
    public void onClick(View v) {
        if(v == add_contactBtn){
            checkPermissionforReadContacts();
        }
        if(v == btn_saveToPhone){
            openSaveToPhoneBookActivity();
        }
        if(v == btn_deleteContact){
            openDeleteContactActivity();
        }
    }

    private void checkPermissionforReadContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!marshMallowPermission.checkPermissionForReadContacts()) {
                marshMallowPermission.requestPermissionForReadContacts();
            } else {
                openAddContactActivity();
            }
        }else {
            openAddContactActivity();
        }
    }

    private void openAddContactActivity() {
        startActivity(new Intent(getActivity(), AddContactActivity.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        contactList.clear();
    }

    private void openSaveToPhoneBookActivity() {
        Intent intent = new Intent(getActivity(), AddContactToPhoneActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("arrayList", (Serializable) contactList);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    private void openDeleteContactActivity() {
        Intent intent = new Intent(getActivity(), DeleteContactActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("arrayList", (Serializable) contactList);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    String str = "";
    @Override
    public void onItemClick(int position, View v) {
        Contact contact = contactList.get(position);
        if(contact.getFullName() != null){
            str = contact.getFullName();
        }
        if(contact.getPhoneNumber() != null){
            str = str + "\n" + contact.getPhoneNumber();
        }
        if(contact.getEmail() != null){
            str = str + "\n" + contact.getEmail();
        }
        str = str.trim();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(new String[]{"Share Contact"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "XMemo");
                        intent.putExtra(Intent.EXTRA_TEXT, str);
                        startActivity(Intent.createChooser(intent, "Share Contact via"));
                    } catch(Exception e) {
                        e.printStackTrace();
                        Log.d("ContactsFragment", "Exception: " + e.getMessage());
                    }
                }
            }
        });
        builder.show();
    }
}
