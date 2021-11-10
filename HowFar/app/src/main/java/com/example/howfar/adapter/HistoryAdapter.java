package com.example.howfar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.model.Participant;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public ArrayList<Participant> participantsList;

    public HistoryAdapter(ArrayList<Participant> list) {
        participantsList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Participant participant = participantsList.get(position);
        holder.historyNicknameTextView.setText(participant.nickname);
        holder.historyDistanceTextView.setText(participant.distanceToLocation.toString());
    }

    @Override
    public int getItemCount() {
        return participantsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView historyNicknameTextView;
        public TextView historyDistanceTextView;

        public ViewHolder(View view) {
            super(view);
            historyNicknameTextView = view.findViewById(R.id.historyNickname);
            historyDistanceTextView = view.findViewById(R.id.historyDistance);
        }
    }

}
