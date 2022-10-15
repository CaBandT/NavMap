package com.example.navmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText emailET, passwordET;
    TextView forgotPasswordTV, goToRegisterTV;
    Button loginBtn;

    private FirebaseAuth mAuth;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        emailET = findViewById(R.id.loginEmailEditText);
        passwordET = findViewById(R.id.loginPasswordEditText);
        forgotPasswordTV = findViewById(R.id.forgotPasswordTextView);
        loginBtn = findViewById(R.id.loginBtn);
        goToRegisterTV = findViewById(R.id.registerTextView);

        mAuth = FirebaseAuth.getInstance();
    }

    public void forgotPasswordOnClick(View view) {
        Toast.makeText(this, "Forgot Password", Toast.LENGTH_SHORT).show();
    }

    public void loginOnClick(View view) {
        try {
            //get the email and password
            String email = emailET.getText().toString().trim();
            String password = passwordET.getText().toString().trim();

            //housekeeping
            if (TextUtils.isEmpty(email))
            {
                emailET.setError("Enter your email");
                emailET.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password))
            {
                passwordET.setError("Enter your password");
                passwordET.requestFocus();
                return;
            }

            //attempt sign-in
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Login Successful");

                        //take to next screen
                        Intent intent = new Intent(LoginActivity.this, NavMain.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                       passwordET.setError("Error logging in!");
                        Log.w(TAG, "Error logging in");

                        //clear boxes
                        passwordET.setText("");
                        passwordET.requestFocus();
                    }
                }
            });
        } catch (Exception ex)
        {
            Toast.makeText(this, "Unable to login!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Catch log in: " + ex.getMessage());
        }
    }

    public void goToRegisterOnClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}