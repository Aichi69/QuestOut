package com.example.questout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class LeaderboardAdapter extends ArrayAdapter<LeaderboardEntry> {
    private Context context;
    private List<LeaderboardEntry> entries;
    private String type;

    public LeaderboardAdapter(Context context, List<LeaderboardEntry> entries, String type) {
        super(context, R.layout.leaderboard_item, entries);
        this.context = context;
        this.entries = entries;
        this.type = type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.leaderboard_item, parent, false);
        }

        LeaderboardEntry entry = entries.get(position);

        TextView rankText = convertView.findViewById(R.id.rankText);
        TextView usernameText = convertView.findViewById(R.id.usernameText);
        TextView valueText = convertView.findViewById(R.id.valueText);

        rankText.setText(String.valueOf(entry.getRank()));
        usernameText.setText(entry.getUsername());
        valueText.setText(entry.getValue() + (type.equals("steps") ? " steps" : " days"));

        // Highlight top 3 positions
        if (entry.getRank() <= 3) {
            rankText.setTextColor(context.getResources().getColor(
                entry.getRank() == 1 ? R.color.gold :
                entry.getRank() == 2 ? R.color.silver :
                R.color.bronze
            ));
        }

        return convertView;
    }
} 