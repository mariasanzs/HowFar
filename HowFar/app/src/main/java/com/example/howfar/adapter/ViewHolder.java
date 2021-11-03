package com.example.howfar.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.activities.Place;

public class ViewHolder extends RecyclerView.ViewHolder {

    // Holds references to individual place views
    Context context;
    TextView title;
    View cardLayout;

    public ViewHolder(Context ctxt, View placeView) {
        super(placeView);
        context = ctxt;
        title = placeView.findViewById(R.id.title);
        cardLayout = placeView.findViewById(R.id.cardview);
    }

    void bindValues(Place place) {
        // give values to the elements contained in the place view
        title.setText(place.getTitle());
    }
}
