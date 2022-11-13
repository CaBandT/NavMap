package com.example.navmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class BookmarkAdapter extends ArrayAdapter<Bookmark> {
    private Context mContext;
    private int mResource;

    public BookmarkAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Bookmark> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        convertView = layoutInflater.inflate(mResource, parent, false);

        TextView tvName = convertView.findViewById(R.id.lrbName);
        TextView tvCoOrds = convertView.findViewById(R.id.lrbCoords);

        tvName.setText(getItem(position).getName());
        String coOrds = getItem(position).getLatitude() + ", " + getItem(position).getLongitude();
        tvCoOrds.setText(coOrds);

        FloatingActionButton fabGoTo = convertView.findViewById(R.id.lrbFabGoTo);

        fabGoTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "This is #" + position, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}