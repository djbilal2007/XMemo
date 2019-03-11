package com.app.xmemo.xmemo_image.activity;

import android.content.ContentProviderOperation;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.adapter.ContactSaveToPhoneAdapter;
import com.app.xmemo.xmemo_image.bean.Contact;
import com.app.xmemo.xmemo_image.permission.MarshMallowPermission;
import com.app.xmemo.xmemo_image.utils.Utils;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AddContactToPhoneActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton saveToPhone, select_all;

    private List<Contact> contactList;
    private RecyclerView recyclerView;
    private ContactSaveToPhoneAdapter adapter;

    private MarshMallowPermission marshMallowPermission;
    private byte[] bitmapByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_to_phone);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Utils.initLoading(this);

        marshMallowPermission = new MarshMallowPermission(this);

        Bundle bundle = getIntent().getBundleExtra("bundle");

        contactList = (ArrayList<Contact>) bundle.getSerializable("arrayList");

        saveToPhone = (ImageButton) findViewById(R.id.btn_saveToPhoneBook);
        saveToPhone.setOnClickListener(this);

        select_all = (ImageButton) findViewById(R.id.select_all_contact);
        select_all.setOnClickListener(this);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView_savePhone);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new ContactSaveToPhoneAdapter(this, contactList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if(v == saveToPhone){
            checkPermissionforWriteContacts();
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

    private void checkPermissionforWriteContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!marshMallowPermission.checkPermissionForWriteContacts()) {
                marshMallowPermission.requestPermissionForWriteContacts();
            } else {
                getSelectedContacts();
            }
        }else {
            getSelectedContacts();
        }
    }

    private void getSelectedContacts() {
        List<Contact> contacts = adapter.getStudentList();
        List<Contact> contactsToSave = new ArrayList<>();

        for(int i=0; i<contacts.size(); i++){
            Contact contact = contacts.get(i);
            if(contact.isSelected() == true){
                contactsToSave.add(contact);
            }
        }
        if(contactsToSave.size() >0){
            saveContactsToPhoneBook(contactsToSave);
            //Utils.showToast(this, "Size of selected contacts: " + contactsToSave.size());
        }else {
            Utils.showToast(this, "Please select some contacts to save");
        }
    }

    private void saveContactsToPhoneBook(List<Contact> contactsToSave) {
        Utils.showLoading("Saving to Phonebook....");
        for(Contact contact : contactsToSave) {

            String DisplayName = contact.getFullName();
            String MobileNumber = contact.getPhoneNumber();
            String emailID = contact.getEmail();
            final String imageUrl = contact.getImageUrl();

            final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            //------------------------------------------------------ Names
            if (DisplayName != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                DisplayName).build());
            }

            //------------------------------------------------------ Mobile Number
            if (MobileNumber != null) {
                ops.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }

            //------------------------------------------------------ Email
            if (emailID != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build());
            }

            new AsyncTask<Void, Void, Void>(){

                Bitmap bitmap = null;

                @Override
                protected Void doInBackground(Void... params) {
                    //   Looper.prepare();
                    try {
                        bitmap = Glide.with(AddContactToPhoneActivity.this)
                                .load(imageUrl)
                                .asBitmap()
                                .into(100, 100)
                                .get();
                    }catch (Exception e){
                        e.getMessage();
                        Log.d("AddContact", "Exception: " + e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if(bitmap != null) {
                        bitmapByteArray = toByteArray(bitmap);
                        Log.d("AddContact", "Image Downloaded");

                        // add the photo
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, bitmapByteArray)
                                .build());
                    }
                    // Asking the Contact provider to create a new contact
                    try {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddContactToPhoneActivity.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }

        Utils.hideLoading();
        if(contactsToSave.size() == 1){
            Toast.makeText(this, "1 Contact stored.", Toast.LENGTH_SHORT).show();
        }else if(contactsToSave.size() > 1){
            Toast.makeText(this, "" + contactsToSave.size() + " Contacts stored.", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }
}
