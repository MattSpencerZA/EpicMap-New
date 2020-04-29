package com.example.EpicMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class RegisterTwo extends AppCompatActivity{
    TextView text1, text2;
    Button register;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase database;
    DatabaseReference reference, reference2;
    userPrefs userPrefs;
    transportPrefs transportPrefs;
    int maxid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_two);
        //spinner activity
        Spinner spinner = findViewById(R.id.mySpinner);
        Spinner spinnerOne = findViewById(R.id.spinner);
        text1 = findViewById(R.id.reg_text_2);
        text2 = findViewById(R.id.reg_text_3);
        register = findViewById(R.id.btnRegister);
        userPrefs = new userPrefs();
        transportPrefs = new transportPrefs();
        reference = FirebaseDatabase.getInstance().getReference().child("TransportPref");
        reference2 = FirebaseDatabase.getInstance().getReference().child("System");

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
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    maxid = (int)dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    maxid = (int)dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        register.setOnClickListener(view -> {
            userPrefs.setSpinner(spinner.getSelectedItem().toString());
            transportPrefs.setSpinner2(spinnerOne.getSelectedItem().toString());
            int dbEntry = maxid;
            if(reference!=null) {
                reference.child(String.valueOf(dbEntry)).setValue(userPrefs).toString();
                reference2.child(String.valueOf(dbEntry)).setValue(transportPrefs).toString();

                maxid++;
            }
            else {
                Toast.makeText(this, "Error !", Toast.LENGTH_SHORT).show();
                }
            startActivity(new Intent(RegisterTwo.this, MainActivity.class));
        });
    }
}

