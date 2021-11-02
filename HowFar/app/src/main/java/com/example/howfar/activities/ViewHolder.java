package com.example.howfar.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;

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
