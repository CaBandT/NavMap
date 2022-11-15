package com.cabandt.navmap.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cabandt.navmap.Bookmark;
import com.cabandt.navmap.BookmarkAdapter;
import com.cabandt.navmap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarksFragment extends Fragment {

    private Activity activity;
    ListView lvBookmarks;
    LinearLayout layoutNoBookmarks;
    ProgressBar progressBar;

    ArrayList<Bookmark> bookmarkArray;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userUid;

    private final String TAG = "fragment_bookmarks";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        lvBookmarks = activity.findViewById(R.id.lvBookmarks);
        layoutNoBookmarks = activity.findViewById(R.id.layoutNoBookmarks);
        progressBar = activity.findViewById(R.id.bookmarksProgressBar);

        //Firebase instantiations
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userUid = mAuth.getCurrentUser().getUid();

        loadBookmarksFromDB();
    }

    private void populateBookmarks(){
        bookmarkArray = new ArrayList<>();

        try {
            db.collection("users")
                    .document(userUid)
                    .collection("bookmarks")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult())
                                {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    String id = document.getId();
                                    String name = document.get("name").toString();
                                    String latitude = document.get("lat").toString();
                                    String longitude = document.get("lng").toString();

                                    bookmarkArray.add(new Bookmark(id, name, latitude, longitude));
                                }

                                if (bookmarkArray.size() < 1)
                                {
                                    Log.d(TAG, "No bookmarks to show");
                                    layoutNoBookmarks.setVisibility(View.VISIBLE);
                                } else
                                {
                                    Log.d(TAG, "Showing bookmarks");
                                    //Populate List View
                                    try {
                                        BookmarkAdapter bookmarkAdapter = new BookmarkAdapter(activity, R.layout.list_row_bookmark, bookmarkArray);
                                        lvBookmarks.setAdapter(bookmarkAdapter);
                                    }catch (Exception e)
                                    {
                                        Log.e(TAG, "Failed to populate bookmarks...");
                                        e.printStackTrace();
                                    }
                                }
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBookmarksFromDB()
    {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                //pre-execute
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                //background
                try {
                    populateBookmarks();
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't load bookmarks");
                    e.printStackTrace();
                }
            }
        });
    }
}