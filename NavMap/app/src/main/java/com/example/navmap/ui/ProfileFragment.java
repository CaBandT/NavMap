package com.example.navmap.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.navmap.LoginActivity;
import com.example.navmap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private Activity activity;

    Spinner spinner;
    TextView Nametxt, Emailtxt;
    Button logout;
    FirebaseAuth auth;
    FirebaseUser user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        logout = activity.findViewById(R.id.btnLogout);
        spinner = activity.findViewById(R.id.spinner);

        activity = getActivity();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            Nametxt = activity.findViewById(R.id.displayName);
            Emailtxt = activity.findViewById(R.id.displayEmail);
            // Name, email address, and profile photo Url

            String name = auth.getCurrentUser().getDisplayName();
            Log.d("Profile name", "name: " + name);
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            Emailtxt.setText(email);
            Nametxt.setText(name);
            boolean emailVerified = user.isEmailVerified();

            String uid = user.getUid();

        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(activity, LoginActivity.class);
                startActivity(intent);
                activity.finish();
            }
        });
    }
}