package com.example.navmap;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText nameET, emailET, passwordET, confirmPasswordET;
    TextView backToLoginTV;
    Button registerBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog.Builder alertBuilder;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        nameET = findViewById(R.id.registerNameEditText);
        emailET = findViewById(R.id.registerEmailEditText);
        passwordET = findViewById(R.id.registerPasswordEditText);
        confirmPasswordET = findViewById(R.id.registerConfirmPasswordEditText);
        registerBtn = findViewById(R.id.registerBtn);
        backToLoginTV = findViewById(R.id.backToLoginTextView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void goToLoginOnClick(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void registerOnClick(View view) {
        try {
            String name = nameET.getText().toString().trim();
            String email = emailET.getText().toString().trim();
            String password = passwordET.getText().toString().trim();
            String confirmPassword = confirmPasswordET.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                nameET.setError("Enter your name");
                nameET.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailET.setError("Enter an email address");
                emailET.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordET.setError("Enter a Password");
                passwordET.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                confirmPasswordET.setError("Re-enter the Password");
                confirmPasswordET.requestFocus();
                return;
            }
            //range checks --> dont allow less than 6
            if (password.length() < 6) {
                passwordET.setError("Password must be 6 characters or more");
                passwordET.requestFocus();
                return;
            }
            if (!confirmPassword.equals(password)) {
                confirmPasswordET.setError("Passwords don't match!");
                confirmPasswordET.requestFocus();
                return;
            }

            //create user
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser fbUser = mAuth.getCurrentUser();

                        //stores users name in firebaseAuth
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(name)
                                .build();
                        fbUser.updateProfile(profileChangeRequest);

                        //Pushing user details into a hashmap
                        Map<String, Object> userSettings = new HashMap<>();
                        userSettings.put("landmarkPreference", "Any");
                        userSettings.put("measurementSystem", "Metric");

                        //Add user info to Firestore DB - https://firebase.google.com/docs/firestore/manage-data/add-data
                        db.collection("users")
                                .document(mAuth.getCurrentUser().getUid())
                                .set(userSettings)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "User Successfully added to Firestore!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error adding user to Firestore!");
                                        simpleAlert("Registration Error!",
                                                "Please try register again later.");
                                        mAuth.getCurrentUser().delete();
                                        return;
                                    }
                                });
                        //Confirm Registration success
                        Toast.makeText(RegisterActivity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();

                        //redirect to login
                        Intent intent = new Intent(RegisterActivity.this, NavMain.class);
                        startActivity(intent);
                        finish();
                    } else {
                        simpleAlert("Registration Error!",
                                "Please try register again later.");
                        Log.e(TAG, "Unable to Register - " + task.getException().getMessage());
                    }
                }
            });
        }catch (Exception ex)
        {
            simpleAlert("Registration Error!",
                    "Please try register again later.");
            Log.e(TAG, "Unable to Register - " + ex.getMessage());
        }
    }

    public void simpleAlert(String title, String message)
    {
        alertBuilder.setTitle(title).
                setMessage(message).
                setCancelable(false).
                setPositiveButton("Ok", null);

        alertBuilder.show();
    }
}