package com.example.pick_contact;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    // UI views
    private ImageView thumbnailIv;
    private TextView contactTv;
    private FloatingActionButton addFab;
    private static final int CONTACT_PERMISSION_CODE = 1;
    private static final int CONTACT_PICK_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init UI views
        thumbnailIv = findViewById(R.id.thumbnailIv);
        contactTv = findViewById(R.id.contactTv);
        addFab = findViewById(R.id.addFab);

        //handle click to pick contact
    addFab.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
            //first we need to check Read_Contact_Permission
            if(checkContactPermission()){
                //Permission Granted, pick contact
                pickContactIntent();
            }
            else{
                //Permission not granted, request
                requestContactPermission();
            }
        }
    });
    }
    private boolean checkContactPermission(){
        //check if contact permission was granted or not
        boolean result = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS) == (PackageManager.PERMISSION_GRANTED
        );
        return result; //true if permission granted, false if not
    }
    private void requestContactPermission(){
        //permissions to request
        String[] permission = {Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this, permission, CONTACT_PERMISSION_CODE);
    }
    private void pickContactIntent(){
        //Intent to pick contact
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //handle permission request result
        if(requestCode == CONTACT_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission granted, can pick a contact now
                pickContactIntent();
            }
            else {
                //Permission denied
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //handle intent results
        if(resultCode == RESULT_OK){
            //calls when a user click a contact from list
            if (requestCode == CONTACT_PICK_CODE){
                contactTv.setText("");
                Cursor cursor1, cursor2;
                //get data from intent
                Uri uri = data.getData();
                cursor1 = getContentResolver().query(uri, null, null, null, null);
                if(cursor1.moveToFirst()){
                    //get contact details
                    @SuppressLint("Range") String contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                    @SuppressLint("Range") String contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    @SuppressLint("Range") String contactThumbnail = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    @SuppressLint("Range") String idResults = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultHold = Integer.parseInt(idResults);
                    contactTv.append("ID: "+contactId);
                    contactTv.append("\nName: "+contactName);
                    if(idResultHold == 1){
                        cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId,
                                null,
                                null
                        );
                        //a contact may have multiple phone numbers
                        while(cursor2.moveToNext()){
                            // get phone number
                            @SuppressLint("Range") String contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // set details

                            contactTv.append("\nPhone: "+contactNumber);
                            //before setting image, check if have or not
                            if(contactThumbnail != null){
                                thumbnailIv.setImageURI(Uri.parse(contactThumbnail));
                            }
                            else{
                                thumbnailIv.setImageResource(R.drawable.ic_person);
                            }
                        }
                        cursor2.close();
                    }
                    cursor1.close();
                }
            }
        }
        else{
            //calls when user clicks back button I don't pick contact
        }
    }
}