package com.example.howfar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;

import java.util.HashMap;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private HashMap<String, String> history;

    public HistoryAdapter(HashMap<String, String> dataSet) {
        history = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Create View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history, parent, false);

        return new ViewHolder(view);
    }

    public void add(String nickname, String distance) {
        history.put(nickname, distance);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.historyNicknameTextView.setText(history.get(position));
        holder.historyDistanceTextView.setText(history.get(position));

    }

    @Override
    public int getItemCount() {
        return history.size();
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
