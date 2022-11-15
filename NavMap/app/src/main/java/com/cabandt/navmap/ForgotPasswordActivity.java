package com.cabandt.navmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.io.DataOutputStream;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etResetEmail;
    Button btnResetPassword;
    TextView btnBackToLogin;
    ProgressBar progressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etResetEmail = findViewById(R.id.etRPEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void resetPassword() {
        String email = etResetEmail.getText().toString().trim();

        if (email.isEmpty()){
            etResetEmail.setError("Email is required!");
            etResetEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etResetEmail.setError("Email not valid! ");
            etResetEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                boolean userExists = task.getResult().getSignInMethods().isEmpty();

                if (userExists){
                    progressBar.setVisibility(View.GONE);
                    etResetEmail.setError("User not found!");
                    etResetEmail.requestFocus();
                    return;
                } else {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            
                            if (task.isSuccessful()){
                                simpleAlert("Email Sent", "Please check your email to reset your password.");
                                etResetEmail.setText("");
                                etResetEmail.clearFocus();
                            } else {
                                simpleAlert("Reset Error", "Couldn't send reset request, please try again later.");
                            }
                        }
                    });
                }
            }
        });
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