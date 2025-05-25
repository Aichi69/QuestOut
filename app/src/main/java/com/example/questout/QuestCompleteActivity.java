package com.example.questout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class QuestCompleteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_complete);

        TextView xpTextView = findViewById(R.id.xpTextView);
        Button claimButton = findViewById(R.id.claimButton);

        // Get XP from intent
        int totalXP = getIntent().getIntExtra("total_xp", 0);

        // Set XP text
        xpTextView.setText(String.format("%d XP", totalXP));

        // Increment quest streak
        SharedPreferences prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
        int currentStreak = prefs.getInt("questStreak", 0);
        int newStreak = currentStreak + 1;
        prefs.edit().putInt("questStreak", newStreak).apply();

        // Update streak in database
        int userId = prefs.getInt("userId", -1);
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.updateQuestStreak(userId, newStreak);

        // Setup click listener
        claimButton.setOnClickListener(v -> {
            // Add XP to total (system XP)
            int currentXP = prefs.getInt("xp", 0); // Use the 'xp' key for system XP
            int newXP = currentXP + totalXP;
            prefs.edit().putInt("xp", newXP).apply();

            // Update XP and level in database
            dbHelper.updateTotalXP(userId, newXP);
            int level = newXP / 50; // Example: 50 XP per level
            dbHelper.updateUserLevel(userId, level);
            Toast.makeText(this, "Saved to DB: XP=" + newXP + ", Level=" + level + ", Quest Streak=" + newStreak, Toast.LENGTH_SHORT).show();

            // Return to home
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
} 