package com.example.questout;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderboardActivity";
    private ListView leaderboardList;
    private Spinner leaderboardTypeSpinner;
    private TextView titleText;
    private DBHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialize views
        leaderboardList = findViewById(R.id.leaderboardList);
        leaderboardTypeSpinner = findViewById(R.id.leaderboardTypeSpinner);
        titleText = findViewById(R.id.titleText);
        dbHelper = new DBHelper(this);
        prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);

        // Debug: Print all users
        Cursor allUsers = dbHelper.getAllUsers();
        if (allUsers != null) {
            Log.d(TAG, "Total users in database: " + allUsers.getCount());
            if (allUsers.getCount() == 0) {
                Log.d(TAG, "WARNING: No users found in database!");
            }
            while (allUsers.moveToNext()) {
                String name = allUsers.getString(allUsers.getColumnIndex("name"));
                int steps = allUsers.getInt(allUsers.getColumnIndex("steps"));
                int streak = allUsers.getInt(allUsers.getColumnIndex("streak"));
                Log.d(TAG, String.format("User: %s, Steps: %d, Streak: %d", name, steps, streak));
            }
            allUsers.close();
        }

        // Setup spinner
        String[] leaderboardTypes = {"Daily Steps", "Quest Streak"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, leaderboardTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaderboardTypeSpinner.setAdapter(spinnerAdapter);

        // Setup spinner listener
        leaderboardTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLeaderboard(position == 0 ? "steps" : "quest");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateLeaderboard("steps");
            }
        });

        // Initial load
        updateLeaderboard("steps");
    }

    private void updateLeaderboard(String type) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (type.equals("steps")) {
                titleText.setText("Daily Steps Leaderboard");
                cursor = dbHelper.getStepsLeaderboard();
            } else {
                titleText.setText("Quest Streak Leaderboard");
                cursor = dbHelper.getQuestLeaderboard();
            }

            if (cursor != null) {
                Log.d(TAG, "Found " + cursor.getCount() + " entries for " + type + " leaderboard");
                
                if (cursor.getCount() == 0) {
                    Log.d(TAG, "WARNING: No entries found for " + type + " leaderboard!");
                }
                
                if (cursor.moveToFirst()) {
                    do {
                        String username = cursor.getString(cursor.getColumnIndex("name"));
                        int value = type.equals("steps") ? 
                            cursor.getInt(cursor.getColumnIndex("steps")) :
                            cursor.getInt(cursor.getColumnIndex("streak"));
                        int rank = cursor.getInt(cursor.getColumnIndex("rank"));
                        
                        Log.d(TAG, String.format("Adding entry - Rank: %d, User: %s, Value: %d", 
                            rank, username, value));
                        
                        entries.add(new LeaderboardEntry(rank, username, value));
                    } while (cursor.moveToNext());
                }
            } else {
                Log.d(TAG, "ERROR: Cursor is null for " + type + " leaderboard!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating leaderboard: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Create and set adapter
        LeaderboardAdapter adapter = new LeaderboardAdapter(this, entries, type);
        leaderboardList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh leaderboard when returning to activity
        String type = leaderboardTypeSpinner.getSelectedItemPosition() == 0 ? "steps" : "quest";
        updateLeaderboard(type);
    }
} 