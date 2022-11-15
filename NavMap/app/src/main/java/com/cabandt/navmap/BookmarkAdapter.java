package com.cabandt.navmap;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cabandt.navmap.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
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

        Double lat = Double.parseDouble(getItem(position).getLatitude());
        Double lng = Double.parseDouble(getItem(position).getLongitude());
        DecimalFormat df = new DecimalFormat("#.00000");
        String coOrds = df.format(lat) + ",   " + df.format(lng);

        tvCoOrds.setText(coOrds);

        FloatingActionButton fabGoTo = convertView.findViewById(R.id.lrbFabGoTo);

        fabGoTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, BookmarkViewActivity.class);
                intent.putExtra("bId",  getItem(position).getId());
                intent.putExtra("bName",  getItem(position).getName());
                intent.putExtra("bLat",  getItem(position).getLatitude());
                intent.putExtra("bLng",  getItem(position).getLongitude());
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }
}
