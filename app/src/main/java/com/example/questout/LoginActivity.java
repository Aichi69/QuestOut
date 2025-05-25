package com.example.questout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView signupLink;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("hasCompletedOnboarding", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginBtn);
        signupLink = findViewById(R.id.signUpLink);
        dbHelper = new DBHelper(this);

        loginButton.setOnClickListener(v -> handleLogin());
        signupLink.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = dbHelper.authenticateUser(email, password);
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex("id"));
            String userName = cursor.getString(cursor.getColumnIndex("name"));
            String userEmail = cursor.getString(cursor.getColumnIndex("email"));

            // Load all progress for this specific user
            int xp = getIntFromCursor(cursor, "xp", 0);
            int level = getIntFromCursor(cursor, "level", 0);
            int streak = getIntFromCursor(cursor, "streak", 0);
            int questStreak = getIntFromCursor(cursor, "questStreak", 0);
            int steps = getIntFromCursor(cursor, "steps", 0);
            int stepsToday = getIntFromCursor(cursor, "stepsToday", 0);
            int goalSteps = getIntFromCursor(cursor, "goalSteps", 0);
            int totalQuests = getIntFromCursor(cursor, "totalQuests", 0);
            int totalTasks = getIntFromCursor(cursor, "totalTasks", 0);
            int highestStreak = getIntFromCursor(cursor, "highestStreak", 0);
            long lastLoginDate = getLongFromCursor(cursor, "lastLoginDate", 0);
            boolean isQuestActive = getIntFromCursor(cursor, "isQuestActive", 0) == 1;

            // Sync user data from database into SharedPreferences
            SharedPreferences prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.putInt("userId", userId);
            editor.putString("userName", userName);
            editor.putString("userEmail", userEmail);
            
            // Save all values to SharedPreferences
            editor.putInt("xp", xp);
            editor.putInt("level", level);
            editor.putInt("streak", streak);
            editor.putInt("questStreak", questStreak);
            editor.putInt("steps", steps);
            editor.putInt("stepsToday", stepsToday);
            editor.putInt("goalSteps", goalSteps);
            editor.putInt("totalQuests", totalQuests);
            editor.putInt("totalTasks", totalTasks);
            editor.putInt("highestStreak", highestStreak);
            editor.putLong("lastLoginDate", lastLoginDate);
            editor.putBoolean("isQuestActive", isQuestActive);
            editor.apply();

            // Ensure any progress in SharedPreferences is not lost (sync back to DB if higher)
            int spLevel = prefs.getInt("level", level);
            int spStreak = prefs.getInt("streak", streak);
            int spSteps = prefs.getInt("steps", steps);
            int spXP = prefs.getInt("xp", xp);
            if (spLevel > level) dbHelper.updateUserLevel(userId, spLevel);
            if (spStreak > streak) dbHelper.updateUserStreak(userId, spStreak);
            if (spSteps > steps) dbHelper.updateUserSteps(userId, spSteps);
            if (spXP > xp) dbHelper.updateTotalXP(userId, spXP);

            cursor.close();
            String info = "[DB] Level: " + level + ", XP: " + xp + ", Streak: " + streak + ", Steps: " + steps;
            if (level == 0 && xp == 0 && streak == 0 && steps == 0) {
                Toast.makeText(this, "Welcome back, " + userName + "!\nYour data is at zero. Play and claim XP to update your account.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Welcome back, " + userName + "!\n" + info, Toast.LENGTH_LONG).show();
            }
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            cursor.close();
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to safely get int values from cursor
    private int getIntFromCursor(Cursor cursor, String columnName, int defaultValue) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                return cursor.getInt(columnIndex);
            }
        } catch (Exception e) {
            // Log error if needed
        }
        return defaultValue;
    }

    // Helper method to safely get long values from cursor
    private long getLongFromCursor(Cursor cursor, String columnName, long defaultValue) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                return cursor.getLong(columnIndex);
            }
        } catch (Exception e) {
            // Log error if needed
        }
        return defaultValue;
    }
} 