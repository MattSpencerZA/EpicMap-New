package com.example.EpicMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText profileEmail, profileNumber;
    ImageView profileImageView;
    TextView tvSyspref, tvTransPref;
    Spinner sysPref, transportMethod;
    Button saveBtn, deleteAccBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent data = getIntent();
        String emailAddress = data.getStringExtra("email");
        String phoneNum = data.getStringExtra("phoneNum");
        String transportPreference = data.getStringExtra("transPref");
        String systemPreference = data.getStringExtra("sysPref");

        profileEmail = findViewById(R.id.profileEmail);
        profileNumber = findViewById(R.id.profilePhoneNo);
        profileImageView = findViewById(R.id.profileImaveView);
        saveBtn = findViewById(R.id.btnSave);
        deleteAccBtn = findViewById(R.id.btnDeleteAccount);

        tvSyspref = findViewById(R.id.tvSysPref);
        tvTransPref = findViewById(R.id.tvTransPref);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = fAuth.getCurrentUser();

        //spinner activity
        sysPref = findViewById(R.id.spSysPref);
        transportMethod = findViewById(R.id.spTransPref);
        // init spinners
        unitsPreference(sysPref, tvSyspref);
        transportPreference(transportMethod, tvTransPref);

        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImageView);
            }
        });
        
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(profileEmail.getText().toString().isEmpty() || profileNumber.getText().toString().isEmpty() || tvSyspref.getText().toString().equals("Units Preference") || tvTransPref.getText().toString().equals("Transportation Preference")) {
                    Toast.makeText(EditProfileActivity.this, "One or many fields are invalid or empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String email = profileEmail.getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = fStore.collection("users").document(user.getUid());
                        Map<String,Object> edited = new HashMap<>();
                        edited.put("email", email);
                        edited.put("phoneNum", profileNumber.getText().toString());
                        edited.put("sysPref", tvSyspref.getText().toString());
                        edited.put("transPref", tvTransPref.getText().toString());
                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Profile updated! ", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        });
                        Toast.makeText(EditProfileActivity.this, "Email changed!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Email is invalid or already exists", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        deleteAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EditProfileActivity.this);
                alert.setTitle("Are you sure you wish to delete your account?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Task was successful! User has been deleted!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(EditProfileActivity.this, LoginActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                        });
                    }
                });

                alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alert.create();
                alert.show();
            }
        });

        profileEmail.setText(emailAddress);
        profileNumber.setText(phoneNum);
        tvTransPref.setText(transportPreference);
        tvSyspref.setText(systemPreference);


        Log.d(TAG, "onCreate: "+ emailAddress + " " + phoneNum  + " " + transportPreference + " " + systemPreference);
    }

    protected void onActivityResult(int reqestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(reqestCode, resultCode, data);
        if(reqestCode == 1000) {
            if(resultCode == Activity.RESULT_OK) {
                Uri imageuri = data.getData();

                uploadImageToFirebase(imageuri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageuri) {
        final StorageReference fileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageuri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profileImageView)).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed.", Toast.LENGTH_SHORT).show()));
    }

    public void unitsPreference (Spinner spinnerOne, TextView tvSyspref){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.systemPreference, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOne.setAdapter(adapter);

        spinnerOne.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Transportation Preference")){
                    Toast.makeText(EditProfileActivity.this, "Please choose your preference", Toast.LENGTH_SHORT).show();
                } else {
                    tvSyspref.setText(adapterView.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(EditProfileActivity.this, "Please make a selection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void transportPreference (Spinner spinner, TextView tvTransPref){
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.transportPreference, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Units Preference")){
                    Toast.makeText(EditProfileActivity.this, "Please choose your preference", Toast.LENGTH_SHORT).show();
                } else {
                    tvTransPref.setText(adapterView.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(EditProfileActivity.this, "Please make a selection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
