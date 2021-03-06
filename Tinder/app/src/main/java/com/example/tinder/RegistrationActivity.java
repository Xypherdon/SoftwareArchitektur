package com.example.tinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private Button mRegister;

    private EditText mEmail, mPassword, mName;

    private RadioGroup mGenderRadioGroup, mReligionRadioGroup;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mRegister = (Button) findViewById(R.id.register);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mName = (EditText) findViewById(R.id.name);
                mGenderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
                mReligionRadioGroup = (RadioGroup) findViewById(R.id.religionRadioGroup);

                int selectedGenderId = mGenderRadioGroup.getCheckedRadioButtonId();
                int selectedReligionId = mReligionRadioGroup.getCheckedRadioButtonId();

                final RadioButton genderRadioButton = (RadioButton) findViewById(selectedGenderId);
                final RadioButton religionRadiobutton = findViewById(selectedReligionId);

                if (genderRadioButton.getText() == null) {
                    return;
                }
                if (religionRadiobutton.getText() == null) {
                    return;
                }


                final String email = mEmail.getText().toString();
                final String passworf = mPassword.getText().toString();
                final String name = mName.getText().toString();
                final String religion = religionRadiobutton.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, passworf).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                        } else {
                            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            String gender = genderRadioButton.getText().toString();
                            DatabaseReference currentNameOfUserDB = FirebaseDatabase.getInstance().getReference().child("users").child(gender).child(userId).child("name");
                            currentNameOfUserDB.setValue(name);
                            DatabaseReference currentReligionOfUserDB = FirebaseDatabase.getInstance().getReference().child("users").child(gender).child(userId).child("religion");
                            currentReligionOfUserDB.setValue(religion);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}
