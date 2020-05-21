package com.example.EpicMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterTwo extends AppCompatActivity{
    TextView text1, text2;
    EditText etphone;
    Button register;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore fstore;
    userPrefs userPrefs;
    transportPrefs transportPrefs;
    int maxid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_two);
        //spinner activity
        Spinner spinner = findViewById(R.id.spTranspref);
        Spinner spinnerOne = findViewById(R.id.spSyspref);
        text1 = findViewById(R.id.reg_text_2);
        text2 = findViewById(R.id.reg_text_3);
        register = findViewById(R.id.btnRegister);
        etphone = findViewById(R.id.etPhone);
        userPrefs = new userPrefs();
        transportPrefs = new transportPrefs();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.systemPreference, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOne.setAdapter(adapter);

        spinnerOne.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Transportation Preference")){
                    Toast.makeText(RegisterTwo.this, "Please choose your preference", Toast.LENGTH_SHORT).show();
                } else {
                    text1.setText(adapterView.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(RegisterTwo.this, "Please make a selection!", Toast.LENGTH_SHORT).show();
            }
        });


        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.userPreference, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("System Preference")){
                    Toast.makeText(RegisterTwo.this, "Please choose your preference", Toast.LENGTH_SHORT).show();
                } else {
                    text2.setText(adapterView.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        register.setOnClickListener(view -> {
            userPrefs.setSpinner(spinner.getSelectedItem().toString());
            transportPrefs.setSpinner2(spinnerOne.getSelectedItem().toString());
            String phoneNumber = etphone.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber)) {
                etphone.setError("Please enter your phone number");
            }
            if (phoneNumber.length() < 10){
                etphone.setError("Please enter a valid phone number");
            }

        });
    }
}

