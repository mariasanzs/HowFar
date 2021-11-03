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

    private static final String TAG = "ListOfItems, MyAdapter";

    private List<Place> places = new ArrayList<>();
    Context context;


    public RecyclerViewAdapter(Context ctxt, List<Place> listofplaces) {
        super();
        context = ctxt;
        places = listofplaces;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method has to actually inflate the item view and return the view holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_layout, parent, false);
        return new ViewHolder(context, v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // this method actually gives values to the elements of the view holder
        // (values corresponding to the item in 'position')
        final Place place = places.get(position);
        holder.bindValues(place);
        holder.cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Hola", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(this,Class.ConfirmMeetActivity);
                intent2.putExtra("longitude", place.getLongitude());
                intent2.putExtra("latitude", place.getLatitude());
                startActivity(intent2);

            }
        });


    }

    @Override
    public int getItemCount() {
        return places.size();
    }


}
