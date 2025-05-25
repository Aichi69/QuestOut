package com.example.questout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.LayoutInflater;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

public class QuestLeaderboardActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private TextView yourStreakText;
    private LinearLayout leaderboardContainer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private int questStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_leaderboard);

        prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
        yourStreakText = findViewById(R.id.yourStreakText);
        leaderboardContainer = findViewById(R.id.leaderboardContainer);

        // Initialize streak
        questStreak = prefs.getInt("questStreak", 0);

        // Add your entry
        addLeaderboardEntry("YOU", questStreak, 0, true);

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

    private void updateStreakDisplay() {
        // Get latest streak from SharedPreferences
        questStreak = prefs.getInt("questStreak", 0);
        
        // Update bottom section
        if (yourStreakText != null) {
            yourStreakText.setText(questStreak + " DAYS STREAK");
        }

        // Update leaderboard entry
        if (leaderboardContainer != null && leaderboardContainer.getChildCount() > 0) {
            View entryView = leaderboardContainer.getChildAt(0);
            TextView stepsText = entryView.findViewById(R.id.stepsText);
            if (stepsText != null) {
                stepsText.setText(questStreak + " DAYS STREAK");
            }
        }
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