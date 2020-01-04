package com.example.tinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField;

    private Button mBack, mConfirm;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    private String userId, name, phone, profileImageURL;

    private String userSex;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userSex = getIntent().getExtras().getString("userSex");

        //user fields
        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);

        //profile image
        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        //Buttons
        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirmSettings);

        //get current user Id
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userSex).child(userId);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //allow user to pick from the phone
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }


    private void getUserInfo(){
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //set name
                    if (map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }

                    //set phone
                    if (map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }

                    //set Image
                    if (map.get("profileImageUrl")!=null){
                        profileImageURL = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageURL).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {
        name = mNameField.getText().toString();
        phone = mPhoneField.getText().toString();

        final Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);

        //Update database
        mCustomerDatabase.updateChildren(userInfo);

        if (resultUri!=null){
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);

            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //compress the image
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //upload failed
                    finish();
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            final Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    Map userInfo = new HashMap();
                                    userInfo.put("profileImageUrl",imageUrl);
                                    mCustomerDatabase.updateChildren(userInfo);
                                }
                            });
                        }
                    }
                }
            });
        }else {
            finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            //image location on the phone
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}
