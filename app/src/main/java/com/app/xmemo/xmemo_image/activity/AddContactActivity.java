package com.app.xmemo.xmemo_image.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.adapter.ContactsAdapter;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;
import com.app.xmemo.xmemo_image.utils.Constants;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.util.List;

public class AddContactActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton add_contactBtn, send_contactBtn;

    private static final int REQUEST_CONTACT = 101;
    private ContactsAdapter adapter;
    private RecyclerView recyclerView;

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private static String userUID = "";

    private MarshMallowPermission marshMallowPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        //Upload Notification
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Uploading Contacts")
                .setSmallIcon(Utils.getNotificationIcon());
        //

        marshMallowPermission = new MarshMallowPermission(this);

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView_addContact);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        add_contactBtn = (ImageButton)findViewById(R.id.add_contactActivityBtn);
        add_contactBtn.setOnClickListener(this);

        send_contactBtn = (ImageButton)findViewById(R.id.send_contactBtn);
        send_contactBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == add_contactBtn){
            pickContacts();
        }
        if(v == send_contactBtn){
            uploadToServer();
        }
    }

    private void pickContacts() {
        Intent intent = new Intent(this, ContactPickerActivity.class)
                //.putExtra(ContactPickerActivity.EXTRA_THEME, R.style.ContactPicker_Theme_Dark)// : R.style.ContactPicker_Theme_Light)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CONTACT && resultCode == RESULT_OK && data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)){
            List<Contact> contacts = (List<Contact>)data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            Constants.contactList.addAll(contacts);
            updateListView();
        }
    }

    private void updateListView() {
        adapter = new ContactsAdapter(this, Constants.contactList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //Upload to Firebase
    private boolean uploadToServer() {
        if (!Utils.isInternetConnected(this)) {
            Utils.showToast(this, getResources().getString(R.string.check_internet_connection));
            return false;
        } else {
            if (Constants.contactList.isEmpty()) {
                Utils.showToast(this, "Please select some contacts to upload");
            }else {
                syncToFirebase();
            }
            return true;
        }
    }

    int size = 0;
    int k = -1;
    private void syncToFirebase() {
        size = Constants.contactList.size();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Utils.showToast(AddContactActivity.this, "Contacts will upload in background");
        startNotificationInTray();


        for(int i=0; i<size; i++) {
            final Contact contact = Constants.contactList.get(i);
            if (Constants.contactList.get(i).getPhotoUri() != null){
                final String fileName = "IMG_" + System.currentTimeMillis();
                StorageReference reference = storageReference.child(userUID + "/uploadedContacts/" + fileName);
                reference.putFile(Constants.contactList.get(i).getPhotoUri())
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Log.d("AddContactActivity", "Download Url: " + downloadUrl.toString());
                                add();
                                saveContactToDatabase(downloadUrl.toString(), fileName, contact.getDisplayName(), contact.getEmail(0), contact.getPhone(0));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.showToast(AddContactActivity.this, "Error Occured. " + e.getMessage());
                        Log.d("AddContactActivity", "Exception: " + e.getMessage());
                    }
                });
            }else {
                add();
                saveContactToDatabase(null, null, contact.getDisplayName(), contact.getEmail(0), contact.getPhone(0));
            }
        }
        finish();
    }

    private void saveContactToDatabase(String imgUrl, String imgName, String displayName, String email, String phone) {
        com.app.xmemo.xmemo_image.bean.Contact contact = new com.app.xmemo.xmemo_image.bean.Contact(imgUrl, imgName, displayName, email, phone);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("uploadedData")
                .child(userUID)
                .child("uploadedContacts")
                .push();

        String key = ref.getKey();
        contact.setKey(key);
        ref.setValue(contact);
    }

    private void add() {
        k++;
        if(k == (size - 1)){
            stopNotificationProgress();
        }
    }

    private void stopNotificationProgress() {
        builder.setContentText("Contacts Upload Complete");
        builder.setProgress(0, 0, false);
        notificationManager.notify(0, builder.build());
    }

    private void startNotificationInTray() {
        builder.setProgress(0, 0, true);
        notificationManager.notify(0, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.contactList.clear();
    }
}
