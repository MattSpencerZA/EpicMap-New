package com.example.EpicMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText etusername, etpassword;
    Button btnregister, btnlogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etusername = findViewById(R.id.etUsername);
        etpassword = findViewById(R.id.etPassword);
        //Buttons
        btnlogin = findViewById(R.id.btnLogin);
        btnregister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
    }
    public void loginMethod(View view) {
        if(view.getId() == R.id.btnLogin){

            String email = etusername.getText().toString().trim();
            String pass = etpassword.getText().toString().trim();

            if(TextUtils.isEmpty(email))
            {
                Toast.makeText(this, "Please enter email!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(pass))
            {
                Toast.makeText(this, "Please enter password!", Toast.LENGTH_SHORT).show();
                return;
            }

            //sign in code
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(LoginActivity.this, "Signed in !!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this, "Failed to sign in!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void registerClick(View view){
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, Register.class));
            }
        });
    }
}
