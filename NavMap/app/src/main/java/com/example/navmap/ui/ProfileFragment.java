package com.example.navmap.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.navmap.LoginActivity;
import com.example.navmap.MainActivity;
import com.example.navmap.R;
import com.example.navmap.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    Spinner spinner;

    private Activity activity;
    TextView Nametxt;
    TextView Emailtxt;
    TextView fragmentMsg;
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
        //SpinnerAdapter adap = new ArrayAdapter<String>(activity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, new String[]{"Historic", "Scenic", "Architectural"});
        //spinner.setAdapter(adap);

        activity = getActivity();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Nametxt = activity.findViewById(R.id.displayName);
            Emailtxt = activity.findViewById(R.id.displayEmail);
            // Name, email address, and profile photo Url
            //String name = user.getDisplayName();
            String name = auth.getCurrentUser().getDisplayName();
            Log.d("Profile name", "name: " + name);
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            //String realName = email.substring(0,6);
            Emailtxt.setText(email);
            Nametxt.setText(name);

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
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