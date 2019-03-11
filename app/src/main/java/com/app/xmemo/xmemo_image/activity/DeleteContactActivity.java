package com.app.xmemo.xmemo_image.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.adapter.ContactSaveToPhoneAdapter;
import com.app.xmemo.xmemo_image.bean.Contact;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DeleteContactActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = DeleteContactActivity.class.getSimpleName();

    private ImageButton deleteContact, select_all;

    private RecyclerView recyclerView;
    private List<Contact> contactList;
    private ContactSaveToPhoneAdapter adapter;
    private static String userUID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_contact);

        Utils.initLoading(this);

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Bundle bundle = getIntent().getBundleExtra("bundle");
        contactList = (ArrayList<Contact>) bundle.getSerializable("arrayList");

        deleteContact = (ImageButton) findViewById(R.id.btn_deleteContact);
        deleteContact.setOnClickListener(this);

        select_all = (ImageButton) findViewById(R.id.select_all_contact_to_delete);
        select_all.setOnClickListener(this);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView_deleteContact);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new ContactSaveToPhoneAdapter(this, contactList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if(v == deleteContact){
            getSelectedContacts();
        }
        if(v == select_all){
            selectAllPressed();
        }
    }

    private void selectAllPressed() {
        for (int i=0; i< adapter.getStudentList().size(); i++){
            adapter.getStudentList().get(i).setSelected(true);
        }
        adapter.notifyDataSetChanged();
    }

    private void getSelectedContacts() {
        List<Contact> contacts = adapter.getStudentList();
        List<Contact> contactsToDelete = new ArrayList<>();

        for(int i=0; i<contacts.size(); i++){
            Contact contact = contacts.get(i);
            if(contact.isSelected() == true){
                contactsToDelete.add(contact);
            }
        }
        if(contactsToDelete.size() >0){
            deleteContactFromFirebase(contactsToDelete);
            //Utils.showToast(this, "Size of selected contacts: " + contactsToSave.size());
        }else {
            Utils.showToast(this, "Please select some contacts to save");
        }
    }

    private void deleteContactFromFirebase(List<Contact> contactsToDelete) {
        Utils.showLoading("Deleting Contacts....");

        for(final Contact contact : contactsToDelete){
            if(contact.getImageUrl() != null || contact.getImageName() != null){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(userUID + "/uploadedContacts/" + contact.getImageName());
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FullScreenActivity", "File Deleted Succesfully from Storage");
                        deleteContactFromDatabase(contact.getKey());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.hideLoading();
                        Utils.showToast(DeleteContactActivity.this, "Exception Occured");
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                });
            }else {
                deleteContactFromDatabase(contact.getKey());
            }
        }
    }

    private void deleteContactFromDatabase(String key) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploadedData/" + userUID + "/uploadedContacts/" + key);
        databaseRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FullScreenActivity", "Data deleted from Database");
                Utils.hideLoading();
                Utils.showToast(DeleteContactActivity.this, "Contacts Deleted");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Data deletion failed. Exception: " + e.getMessage());
                Utils.hideLoading();
                Utils.showToast(DeleteContactActivity.this, "Exception Occured");
            }
        });
    }
}
