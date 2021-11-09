package com.example.howfar.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.model.Place;

public class ViewHolder extends RecyclerView.ViewHolder {

    // Holds references to individual place views
    Context context;
    TextView title;
    View cardLayout;
    private RecyclerViewAdapter.OnClickListener clickListener;

    public ViewHolder(Context ctxt, View placeView, RecyclerViewAdapter.OnClickListener listener) {
        super(placeView);
        context = ctxt;
        title = placeView.findViewById(R.id.title);
        cardLayout = placeView.findViewById(R.id.cardview);
        clickListener = listener;
        placeView.setOnClickListener(view -> this.clickListener.onItemClick(getAdapterPosition()));
    }

    void bindValues(Place place) {
        // give values to the elements contained in the place view
        title.setText(place.getTitle());
    }
}
