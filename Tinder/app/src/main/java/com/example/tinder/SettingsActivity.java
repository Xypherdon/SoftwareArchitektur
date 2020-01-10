package com.example.tinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SettingsActivity extends AppCompatActivity {

    private static final String API_URL = "http://opeapi.ws.pho.to/addtask";
    private static final int REQUEST_CODE = 101;
    private static final String SECRET_API_KEY = "98a07f11ae9c88df35d31e5f5ff32f40";
    private static final String APP_ID = "470ce24349b4ee91c4c7c97e79fca0f2";
    private EditText mNameField, mPhoneField;

    private TextView mCoordinates, mProgressBar;

    private Button mBack, mConfirm, mLocation;

    private SeekBar seekBarRange;

    private ImageView mProfileImage;

    private RadioGroup mReligionRadioGroup;

    private FirebaseAuth mAuth;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private DatabaseReference mCustomerDatabase;

    private String userId, name, phone, profileImageURL, location, range, religion;

    private String userSex;

    private Uri resultUri;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //request location permission
        requestLocationPermission();

        userSex = getIntent().getExtras().getString("userSex");

        //user fields
        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mCoordinates = (TextView) findViewById(R.id.coords);
        mProgressBar = (TextView) findViewById(R.id.progressBar);


        //profile image
        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        //Buttons
        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirmSettings);
        mLocation = (Button) findViewById(R.id.getCurrentLocation);

        //religion
        mReligionRadioGroup = findViewById(R.id.religionRadioGroup);

        //seekbar
        seekBarRange = (SeekBar) findViewById(R.id.seekBarRange);

        //get current user Id
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userSex).child(userId);

        getUserInfo();

        //location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //on change listeners
        seekBarRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                mProgressBar.setText("Range: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
            }
        });

        //on click listeners
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //allow user to pick from the phone
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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

        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();

                    String text = currentLocation.getLatitude() + " " + currentLocation.getLongitude();

                    mCoordinates.setText(text);
                }
            }
        });
    }

    private void getUserInfo() {
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //set name
                    if (map.get("name") != null) {
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }

                    //set phone
                    if (map.get("phone") != null) {
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }

                    //set Image
                    if (map.get("profileImageUrl") != null) {
                        profileImageURL = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageURL).into(mProfileImage);
                    }

                    //set location
                    if (map.get("location") != null) {
                        location = map.get("location").toString();
                        mCoordinates.setText(location);
                    }

                    if (map.get("religion") != null) {
                        religion = map.get("religion").toString();

                        //in caz ca sunt si alte lucruri in radiogroup
                        ArrayList<RadioButton> listOfRadioButtons = new ArrayList<RadioButton>();
                        for (int i = 0; i < mReligionRadioGroup.getChildCount(); i++) {
                            View o = mReligionRadioGroup.getChildAt(i);
                            if (o instanceof RadioButton) {
                                listOfRadioButtons.add((RadioButton) o);
                            }
                        }

                        for (RadioButton radioButton : listOfRadioButtons) {
                            if (radioButton.getText().toString().equals(religion)) {
                                radioButton.setChecked(true);
                            }
                        }
                    }

                    //set progress bar
                    if (map.get("range") != null) {
                        range = map.get("range").toString();
                        mProgressBar.setText("Range: " + range);
                    } else {
                        //set progress bar default
                        mProgressBar.setText("Range: " + seekBarRange.getProgress());
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
        location = mCoordinates.getText().toString();
        range = mProgressBar.getText().toString();
        RadioButton selectedReligion = findViewById(mReligionRadioGroup.getCheckedRadioButtonId());
        religion = selectedReligion.getText().toString();

        final Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("location", location);
        userInfo.put("range", range);
        userInfo.put("religion", religion);

        //Update database
        mCustomerDatabase.updateChildren(userInfo);

        if (resultUri != null) {
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
                                    userInfo.put("profileImageUrl", imageUrl);
                                    mCustomerDatabase.updateChildren(userInfo);
                                }
                            });
                        }
                    }
                }
            });
        } else {
            finish();
        }


    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    private String encodeHMACSHA1(String data, String key) {
        String hmac = "";
        try {

            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(data.getBytes());

            BigInteger hash = new BigInteger(1, digest);
            hmac = hash.toString(16);

            if (hmac.length() % 2 != 0) {
                hmac = "0" + hmac;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return hmac;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            assert data != null;
            //image location on the phone
            Bundle bundle = data.getExtras();
            assert bundle != null;
            Bitmap bitmap = (Bitmap) bundle.get("data");
            assert bitmap != null;
            mProfileImage.setImageBitmap(bitmap);

            StorageReference storageRef = storage.getReference();
            StorageReference profilePicsRef = storageRef.child("profileImages/" + userId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteData = baos.toByteArray();
            UploadTask uploadTask = profilePicsRef.putBytes(byteData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    System.out.println("A dat handicapatu ala cu viteza (eroare upload)");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            final Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    String data = "<image_process_call>\n" +
                                            "  <image_url>" + imageUrl + "</image_url>\n" +
                                            "  <methods_list>\n" +
                                            "    <method>\n" +
                                            "      <name>collage</name>\n" +
                                            "<params>template_name=1230</params>\n" +
                                            "    </method>\n" +
                                            "  </methods_list>\n" +
                                            "</image_process_call>";

                                    String sign_data = encodeHMACSHA1(data, SECRET_API_KEY);


                                    JSONObject jsonBody = new JSONObject();
                                    try {
                                        jsonBody.put("app_id", APP_ID);
                                        jsonBody.put("sign_data", sign_data);
                                        jsonBody.put("data", data);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    final String requestBody = jsonBody.toString();

                                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, API_URL,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    // response
                                                    System.out.println("Success");
                                                    System.out.println(response);
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    System.out.println(error);
                                                }
                                            }) {
                                        @Override
                                        public String getBodyContentType() {
                                            return "application/json; charset=utf-8";
                                        }

                                        @Override
                                        public byte[] getBody(){
                                            try {
                                                return requestBody.getBytes("utf-8");
                                            } catch (UnsupportedEncodingException uee) {
                                                System.out.println("Unsupported Encoding while trying to get the bytes of %s using %s " + requestBody + " utf-8");
                                                return null;
                                            }
                                        }

                                        @Override
                                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                            String responseString = "";
                                            if (response != null) {
                                                responseString = String.valueOf(response.statusCode);
                                                // can get more details such as response.headers
                                            }
                                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                                        }
                                    };
                                    requestQueue.add(stringRequest);

                                    Map userInfo = new HashMap();
                                    userInfo.put("profileImageUrl", imageUrl);
                                    mCustomerDatabase.updateChildren(userInfo);
                                }
                            });
                        }
                    }
                }
            });


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
                break;
        }
    }
}
