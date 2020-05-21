package com.example.EpicMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Register extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    EditText etusername, etpassword, etconfpass;
    Button next;
    ProgressBar progbar;
    FirebaseFirestore fstore;
    TextView tvredirect;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etusername = findViewById(R.id.etUsername);
        etusername.requestFocus();
        etpassword = findViewById(R.id.etPassword);
        etconfpass = findViewById(R.id.etConfirm);
        tvredirect = findViewById(R.id.tvRedirect);

        next = findViewById(R.id.btnNext);

        progbar = findViewById(R.id.progressBar2);
        progbar.setVisibility(View.GONE);
        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //set onclick listener for the next button
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etusername.getText().toString();
                final String password = etpassword.getText().toString().trim();
                final String password2 = etconfpass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    etusername.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    etpassword.setError("Password is required!");
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
                                            Map<String, Object> user
                                            startActivity(new Intent(getApplicationContext(), RegisterTwo.class));
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
}

