package com.example.navmap.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.navmap.R;

public class BookmarksFragment extends Fragment {

    private Activity activity;
    TextView fragmentMsg;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentMsg = activity.findViewById(R.id.text_bookmarks);

        fragmentMsg.setText("This is the bookmarks fragment.");
    }
}