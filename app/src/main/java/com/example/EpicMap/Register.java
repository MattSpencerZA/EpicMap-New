package com.example.EpicMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.R.layout.simple_spinner_item;
import static com.example.EpicMap.R.array.systemPreference;
import static com.example.EpicMap.R.array.userPreference;

public class Register extends AppCompatActivity {

    public static final String TAG = "TAG";
    private FirebaseAuth firebaseAuth;
    private EditText etusername, etpassword, etconfpass, etphone;
    private Button next;
    private ProgressBar progbar;
    private FirebaseFirestore fstore;
    private TextView tvredirect, textview1, textview2;
    private String userID;
    private String unitsPref = "metric";
    private String transportPref = "driving";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etusername = findViewById(R.id.etUsername);
        etusername.requestFocus();
        etpassword = findViewById(R.id.etPassword);
        etconfpass = findViewById(R.id.etConfirm);
        tvredirect = findViewById(R.id.tvRedirect);
        etphone = findViewById(R.id.etPhonenum);

        next = findViewById(R.id.btnNext);

        progbar = findViewById(R.id.progressBar2);
        progbar.setVisibility(View.GONE);

        //spinner activity
        Spinner units = findViewById(R.id.spUnits);
        Spinner transportMethod = findViewById(R.id.spTransportMethod);
        textview1 = findViewById(R.id.textView3);
        textview2 = findViewById(R.id.textView4);

        // init spinners
        unitsPreference(units, textview1);
        transportPreference(transportMethod, textview2);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        //set onclick listener for the next button
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etusername.getText().toString();
                final String password = etpassword.getText().toString().trim();
                final String password2 = etconfpass.getText().toString().trim();
                final String usersPreference = textview1.getText().toString();
                final String systemsPreferecence = textview2.getText().toString();
                final String phoneNumber = etphone.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    etusername.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    etpassword.setError("Password is required!");
                    return;
                }
                if (TextUtils.isEmpty(phoneNumber)) {
                    etphone.setError("Please enter your phone number");
                    return;
                }
                if (phoneNumber.length() < 10){
                    etphone.setError("Please enter a valid phone number");
                    return;
                }
                if(password.length() < 6){
                    etpassword.setText(null);
                    etconfpass.setText(null);
                    Toast.makeText(Register.this, "Please ensure passwords are longer than 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.equals(password2)){

                    progbar.setVisibility(View.VISIBLE);

                    try {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Register.this, "Success!", Toast.LENGTH_SHORT).show();
                                            userID = firebaseAuth.getCurrentUser().getUid();
                                            DocumentReference documentReference = fstore.collection("users").document(userID);
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("email", email);
                                            user.put("userPref", usersPreference);
                                            user.put("sysPref", systemsPreferecence);
                                            user.put("phoneNum", phoneNumber);
                                            //insert to firestore cloud
                                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess: user profile is created for "+userID);
                                                }
                                            });
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        } else {
                                            Toast.makeText(Register.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progbar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    } catch (Exception e){
                        System.out.println(e);
                        Toast.makeText(Register.this, "Something went wrong - please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(Register.this, "Failed to register user!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvredirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

    }

    public void unitsPreference (Spinner spinnerOne, TextView textview1){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.systemPreference, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOne.setAdapter(adapter);

        spinnerOne.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Transportation Preference")){
                    Toast.makeText(Register.this, "Please choose your preference", Toast.LENGTH_SHORT).show();
                } else {
                    textview1.setText(adapterView.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(Register.this, "Please make a selection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void transportPreference (Spinner spinner, TextView textview2){
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.userPreference, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Units Preference")){
                    Toast.makeText(Register.this, "Please choose your preference", Toast.LENGTH_SHORT).show();
                } else {
                    textview2.setText(adapterView.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(Register.this, "Please make a selection!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

