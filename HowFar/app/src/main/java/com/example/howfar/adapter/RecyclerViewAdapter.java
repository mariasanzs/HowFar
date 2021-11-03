package com.example.howfar.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.activities.Place;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
    public interface OnClickListener {
        void onItemClick(int position);
    }

    private static final String TAG = "ListOfItems, MyAdapter";
    private static OnClickListener clickListener;
    private List<Place> places;
    Context context;

    public RecyclerViewAdapter(Context ctxt, List<Place> listofplaces, OnClickListener listener) {
        super();
        context = ctxt;
        places = listofplaces;
        clickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method has to actually inflate the item view and return the view holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_layout, parent, false);
        return new ViewHolder(context, v, clickListener);
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
