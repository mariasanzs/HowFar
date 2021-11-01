package com.example.howfar.activities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = "ListOfItems, MyAdapter";

    private List<Place> places;
    Context context;


    public RecyclerViewAdapter(Context ctxt, List<Place> listofplaces) {
        super();
        context = ctxt;
        places = listofplaces;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method has to actually inflate the item view and return the view holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.id.place_layout, parent, false);
        return new ViewHolder(context, v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // this method actually gives values to the elements of the view holder
        // (values corresponding to the item in 'position')
        final Place place = places.get(position);
        holder.bindValues(place);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
