package com.cabandt.navmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cabandt.navmap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailET, passwordET;
    TextView forgotPasswordTV, goToRegisterTV;
    Button loginBtn;
    ProgressBar progressBar;

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
        progressBar = findViewById(R.id.loginProgressBar);

        mAuth = FirebaseAuth.getInstance();
    }

    public void forgotPasswordOnClick(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
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

            progressBar.setVisibility(View.VISIBLE);
            //attempt sign-in
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful())
                    {
                        Log.d(TAG, "Login Successful");

                        //take to next screen
                        Intent intent = new Intent(LoginActivity.this, NavMain.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        passwordET.setError("Username or password incorrect!");
                        Log.w(TAG, "Error logging in");

                        //clear boxes
                        passwordET.setText("");
                        passwordET.requestFocus();
                    }
                }
            });
        } catch (Exception ex)
        {
            simpleAlert("Login Error", "Something went wrong while logging you in. Please try again later.");
            Log.e(TAG, "Catch log in: " + ex.getMessage());
        }
    }

    public void goToRegisterOnClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void simpleAlert(String title, String message)
    {
        AlertDialog.Builder quickAlertBuilder = new AlertDialog.Builder(this);

        quickAlertBuilder.setTitle(title).
                setMessage(message).
                setCancelable(false).
                setPositiveButton("Ok", null);

        quickAlertBuilder.show();
    }
}