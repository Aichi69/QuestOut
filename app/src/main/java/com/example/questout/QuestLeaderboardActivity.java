package com.example.questout;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.LayoutInflater;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

public class QuestLeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "QuestLeaderboard";
    private SharedPreferences prefs;
    private TextView yourStreakText;
    private LinearLayout leaderboardContainer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private int questStreak = 0;
    private int currentUserId = -1;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_leaderboard);

        prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
        yourStreakText = findViewById(R.id.yourStreakText);
        leaderboardContainer = findViewById(R.id.leaderboardContainer);
        
        // Get current user ID and streak
        currentUserId = prefs.getInt("userId", -1);
        questStreak = prefs.getInt("questStreak", 0);
        
        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Load leaderboard data
        loadLeaderboardData();

        // Update streak display
        updateStreakDisplay();

        // Start periodic updates
        startPeriodicUpdates();

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());
    }

    private void startPeriodicUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateStreakDisplay();
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(updateRunnable);
    }

    private void loadLeaderboardData() {
        // Clear existing entries
        leaderboardContainer.removeAllViews();
        
        // Get quest streak leaderboard from database
        Cursor cursor = null;
        try {
            cursor = dbHelper.getTop10ByQuestStreak();
            
            if (cursor != null) {
                Log.d(TAG, "Found " + cursor.getCount() + " entries for quest streak leaderboard");
                
                if (cursor.getCount() == 0) {
                    Log.d(TAG, "WARNING: No entries found for quest streak leaderboard!");
                    // Add only current user if no other entries
                    addLeaderboardEntry("YOU", questStreak, 0, true);
                } else {
                    // Process all entries from database
                    boolean foundCurrentUser = false;
                    int rank = 0;
                    
                    while (cursor.moveToNext()) {
                        int userId = cursor.getInt(cursor.getColumnIndex("id"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        int streak = cursor.getInt(cursor.getColumnIndex("questStreak"));
                        
                        boolean isCurrentUser = (userId == currentUserId);
                        if (isCurrentUser) {
                            foundCurrentUser = true;
                            // Update the stored streak value
                            questStreak = streak;
                        }
                        
                        // Add entry to leaderboard
                        addLeaderboardEntry(isCurrentUser ? "YOU" : name, streak, rank, isCurrentUser);
                        rank++;
                    }
                    
                    // If current user not in top 10, add them at the bottom
                    if (!foundCurrentUser && currentUserId != -1) {
                        addLeaderboardEntry("YOU", questStreak, rank, true);
                    }
                }
            } else {
                Log.d(TAG, "ERROR: Cursor is null for quest streak leaderboard!");
                // Fallback to just showing current user
                addLeaderboardEntry("YOU", questStreak, 0, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading quest streak leaderboard: " + e.getMessage());
            e.printStackTrace();
            // Fallback to just showing current user
            addLeaderboardEntry("YOU", questStreak, 0, true);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    private void updateStreakDisplay() {
        // Get latest streak from SharedPreferences
        questStreak = prefs.getInt("questStreak", 0);
        
        // Update bottom section
        if (yourStreakText != null) {
            yourStreakText.setText(questStreak + " DAYS STREAK");
        }

        // Update leaderboard - reload data to reflect any changes
        loadLeaderboardData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    private void addLeaderboardEntry(String name, int streak, int rank, boolean isCurrentUser) {
        View entryView = LayoutInflater.from(this).inflate(R.layout.item_leaderboard_entry, leaderboardContainer, false);
        
        TextView rankText = entryView.findViewById(R.id.rankText);
        TextView nameText = entryView.findViewById(R.id.nameText);
        TextView stepsText = entryView.findViewById(R.id.stepsText);

        // Set rank
        String rankStr = (rank + 1) + (rank == 0 ? "ST" : rank == 1 ? "ND" : rank == 2 ? "RD" : "TH");
        rankText.setText(rankStr);
        
        // Set name
        nameText.setText(name);
        if (isCurrentUser) {
            nameText.setTextColor(Color.parseColor("#FF5E9C")); // Pink color for current user
        }

        // Set streak
        stepsText.setText(streak + " DAYS STREAK");

        leaderboardContainer.addView(entryView);
    }
} 