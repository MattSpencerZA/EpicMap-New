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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    EditText etusername, etpassword, etconfpass;
    Button next;
    ProgressBar progbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etusername = findViewById(R.id.etUsername);
        etpassword = findViewById(R.id.etPassword);
        etconfpass = findViewById(R.id.etConfirm);

        next = findViewById(R.id.btnNext);

        progbar = findViewById(R.id.progressBar);
        progbar.setVisibility(View.GONE);
        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        System.out.println(firebaseAuth.toString());
        //set onclick listener for the next button
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etusername.getText().toString();
                final String password = etpassword.getText().toString().trim();
                final String password2 = etconfpass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Please enter email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Please enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password2)){
                    Toast.makeText(Register.this, "Please confirm password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() < 6 || password2.length() < 6){
                    etpassword.setText(null);
                    etconfpass.setText(null);
                    Toast.makeText(Register.this, "Please ensure passwords are longer than 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progbar.setVisibility(View.VISIBLE);

                if(password.equals(password2)){
                    try {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progbar.setVisibility(View.GONE);
                                        System.out.println("im alive!");
                                        progbar.setProgress(100);
                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(getApplicationContext(), RegisterTwo.class));
                                            Toast.makeText(Register.this, "Success!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(Register.this, "Registration failed!", Toast.LENGTH_SHORT).show();
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
    }
}

