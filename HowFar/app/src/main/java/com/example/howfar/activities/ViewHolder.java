package com.example.howfar.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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

    // Holds references to individual item views
    Context context;
    TextView title;

    public ViewHolder(Context ctxt, View itemView) {
        super(itemView);
        context = ctxt;
        title = itemView.findViewById(R.id.title);
    }

    void bindValues(Place place) {
        // give values to the elements contained in the item view
        title.setText(place.getTitle());
    }
}
