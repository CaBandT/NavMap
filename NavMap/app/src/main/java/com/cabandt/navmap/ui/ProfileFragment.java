package com.cabandt.navmap.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.cabandt.navmap.LoginActivity;
import com.cabandt.navmap.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private Activity activity;

    Spinner landmarkSpinner;
    Spinner languageSpinner;
    SwitchCompat imperialSwitch;
    TextView nameTV, emailTV;
    Button logout, save;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private AlertDialog.Builder alertBuilder;

    public SharedPreferences sharedPreferences;

    public static String userpref = "mypref";
    public static final String unitskey = "unitkey";
    public static final String landemarkkey = "landmarkpref";
    public static final String languagekey = "languagepref";

    String landmarkPreference, measurementSystem, languagePreference;

    private static final String TAG = "ProfileFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        logout = activity.findViewById(R.id.btnLogout);
        save = activity.findViewById(R.id.btnSave);

        landmarkSpinner = activity.findViewById(R.id.landmarkSpinner);
        imperialSwitch = activity.findViewById(R.id.unitsToggle);
        languageSpinner = activity.findViewById(R.id.languageSpinner);

        landmarkPreference = "";
        measurementSystem = "";
        languagePreference = "";

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        userpref = auth.getUid();
        sharedPreferences = activity.getSharedPreferences(userpref, Context.MODE_PRIVATE);

        if (user != null) {
            nameTV = activity.findViewById(R.id.displayName);
            emailTV = activity.findViewById(R.id.displayEmail);
            // Name, email address, and profile photo Url

            String name = auth.getCurrentUser().getDisplayName();
            String email = user.getEmail();
            emailTV.setText(email);
            nameTV.setText(name);

            //read settings
            landmarkPreference = sharedPreferences.getString(landemarkkey,"");
            measurementSystem = sharedPreferences.getString(unitskey,"");
            languagePreference = sharedPreferences.getString(languagekey,"");

            showUserSettings();

            /*db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    Log.d("Profile", document.getId() + " => " + document.getData());
                                    landmarkPreference = document.get("landmarkPreference").toString();
                                    measurementSystem = document.get("measurementSystem").toString();
                                    languagePreference = document.get("languagePreference").toString();

                                    showUserSettings();
                                } else
                                {
                                    simpleAlert("Connection Error",
                                            "Unable to retrieve your settings. Please try again later.");
                                    Log.w("Profile", "Unable to retrieve data!");
                                }
                            }
                        }
                    });*/
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserSettings();
            }
        });

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

    private void updateUserSettings() {
        boolean updateFS = false;

        if (imperialSwitch.isChecked() && measurementSystem.equals("metric")){
            measurementSystem = "imperial";
            updateFS = true;
        } else if ((!imperialSwitch.isChecked()) && measurementSystem.equals("imperial")){
            measurementSystem = "metric";
            updateFS = true;
        }

        String spinnerSelection = landmarkSpinner.getSelectedItem().toString();
        if (!spinnerSelection.equals(landmarkPreference)){
            landmarkPreference = spinnerSelection;
            updateFS = true;
        }

        String languageSelection = languageSpinner.getSelectedItem().toString();
        if (!spinnerSelection.equals(languagePreference)){
            languagePreference = languageSelection;
            updateFS = true;
        }

        //update fs if necessary
        if (updateFS){
            try {
                Map<String, Object> userSettings = new HashMap<>();
                userSettings.put("landmarkPreference", landmarkPreference);
                userSettings.put("measurementSystem", measurementSystem);
                userSettings.put("languagePreference",languagePreference);

                db.collection("users")
                        .document(user.getUid())
                        .set(userSettings)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //update shared prefs
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                editor.putString(landemarkkey, landmarkPreference);
                                editor.putString(unitskey, measurementSystem);
                                editor.putString(languagekey,languagePreference);
                                editor.commit();

                                Log.d(TAG, "Settings successfully saved!");
                                Toast.makeText(activity, "Settings Saved", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error saving settings!");
                                simpleAlert("Error Saving!",
                                        "Your settings weren't saved. Please try again later.");
                            }
                        });
            } catch (Exception e){
                Log.e(TAG, "Error: " + e.getMessage());
                simpleAlert("Error Saving!",
                        "Your settings weren't saved. Please try again later.");
            }
        }
    }

    private void showUserSettings() {
        if (measurementSystem.equals("imperial")){
            imperialSwitch.setChecked(true);
        }

        switch (landmarkPreference){
            case "Historical":
                landmarkSpinner.setSelection(1);
                break;
            case "Modern":
                landmarkSpinner.setSelection(2);
                break;
            case "Architectural":
                landmarkSpinner.setSelection(3);
                break;
            default:
                landmarkSpinner.setSelection(0);
                break;
        }

        switch (languagePreference)
        {
            case "Zulu":
                languageSpinner.setSelection(1);
                break;
            default:
                languageSpinner.setSelection(0);
                break;

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