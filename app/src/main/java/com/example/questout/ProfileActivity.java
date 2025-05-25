package com.example.questout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.util.Log;
import android.widget.ImageButton;

public class ProfileActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private static final String TAG = "ProfileActivity";
    private DBHelper dbHelper;
    private EditText profileName, profileBirthday, profileEmail;
    private Button editSaveBtn;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
        dbHelper = new DBHelper(this);

        // Initialize views
        profileName = findViewById(R.id.profileName);
        profileBirthday = findViewById(R.id.profileBirthday);
        profileEmail = findViewById(R.id.profileEmail);
        editSaveBtn = findViewById(R.id.editSaveBtn);

        // Set up back button
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            if (isEditMode) {
                // If in edit mode, show confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Do you want to discard them?")
                    .setPositiveButton("Discard", (dialog, which) -> {
                        // Discard changes and exit
                        isEditMode = false;
                        editSaveBtn.setText("EDIT");
                        setEditTextsEditable(false);
                        loadUserData(); // Reload original data
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            } else {
                // If not in edit mode, just finish the activity
                setResult(RESULT_OK); // Set result to refresh HomeActivity
                finish();
            }
        });

        // Set EditText fields as non-editable by default
        setEditTextsEditable(false);

        // Load user data
        loadUserData();

        // Set up Edit/Save button click listener
        editSaveBtn.setOnClickListener(v -> {
            if (!isEditMode) {
                // Switch to edit mode
                isEditMode = true;
                editSaveBtn.setText("SAVE");
                setEditTextsEditable(true);
            } else {
                // Save changes and switch back to view mode
                isEditMode = false;
                editSaveBtn.setText("EDIT");
                setEditTextsEditable(false);
                saveUserData();
            }
        });

        Button signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            // Save all user progress to the database before clearing session
            int userId = prefs.getInt("userId", -1);
            int xp = prefs.getInt("xp", 0);
            int level = prefs.getInt("level", 0);
            int streak = prefs.getInt("streak", 0); // This is the daily login streak
            int questStreak = prefs.getInt("questStreak", 0);
            int steps = prefs.getInt("steps", 0);
            int stepsToday = prefs.getInt("stepsToday", 0);
            int goalSteps = prefs.getInt("goalSteps", 0);
            int totalQuests = prefs.getInt("totalQuests", 0);
            int totalTasks = prefs.getInt("totalTasks", 0);
            int highestStreak = prefs.getInt("highestStreak", 0);
            long lastLoginDate = prefs.getLong("lastLoginDate", 0);
            boolean isQuestActive = prefs.getBoolean("isQuestActive", false);

            // Update core stats using existing methods
            dbHelper.updateTotalXP(userId, xp);
            dbHelper.updateUserLevel(userId, level);
            dbHelper.updateUserStreak(userId, streak); // Save daily login streak
            dbHelper.updateUserSteps(userId, steps);
            dbHelper.updateUserStats(userId, level, xp, steps);
            dbHelper.updateStepGoal(userId, goalSteps); // Save step goal
            // Save last login date in SharedPreferences only
            prefs.edit().putLong("lastLoginDate", lastLoginDate).apply();

            // Now clear only session data (SharedPreferences), not the database
            prefs.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        int xp = prefs.getInt("xp", 0);
        int level = xp / 50;
        int xpForNextLevel = (level + 1) * 50;
        int xpThisLevel = xp - (level * 50);

        TextView profileLevel = findViewById(R.id.profileLevel);
        ProgressBar profileXpProgressBar = findViewById(R.id.profileXpProgressBar);
        TextView profileXpText = findViewById(R.id.profileXpText);
        TextView profileAchievements = findViewById(R.id.profileAchievements);

        profileLevel.setText(String.valueOf(level));
        profileXpProgressBar.setMax(xpForNextLevel - (level * 50));
        profileXpProgressBar.setProgress(xpThisLevel);
        profileXpText.setText(xpThisLevel + " / " + (xpForNextLevel - (level * 50)) + " XP to next level");

        int achievements = prefs.getInt("achievements", 0);
        profileAchievements.setText(" • " + achievements + " Achievements");
    }

    private void setEditTextsEditable(boolean editable) {
        profileName.setEnabled(editable);
        profileBirthday.setEnabled(editable);
        profileEmail.setEnabled(editable);
    }

    private void saveUserData() {
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e(TAG, "No user ID found in preferences!");
            return;
        }

        String name = profileName.getText().toString().trim();
        String birthday = profileBirthday.getText().toString().trim();
        String email = profileEmail.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty()) {
            android.widget.Toast.makeText(this, "Name and email cannot be empty", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Update user data in database
            dbHelper.updateUserProfile(userId, name, email, birthday);
            
            // Update SharedPreferences with new name
            prefs.edit().putString("userName", name).apply();
            
            // Show success message
            android.widget.Toast.makeText(this, "Profile updated successfully", android.widget.Toast.LENGTH_SHORT).show();
            
            // Switch back to view mode
            isEditMode = false;
            editSaveBtn.setText("EDIT");
            setEditTextsEditable(false);

            // Set result to refresh HomeActivity
            setResult(RESULT_OK);
        } catch (Exception e) {
            Log.e(TAG, "Error updating profile: " + e.getMessage());
            android.widget.Toast.makeText(this, "Error updating profile", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e(TAG, "No user ID found in preferences!");
            return;
        }

        Cursor userCursor = dbHelper.getUserById(userId);
        if (userCursor != null && userCursor.moveToFirst()) {
            int nameIndex = userCursor.getColumnIndex("name");
            int emailIndex = userCursor.getColumnIndex("email");
            int birthdayIndex = userCursor.getColumnIndex("birthday");

            String username = nameIndex >= 0 ? userCursor.getString(nameIndex) : "";
            String email = emailIndex >= 0 ? userCursor.getString(emailIndex) : "";
            String birthday = birthdayIndex >= 0 ? userCursor.getString(birthdayIndex) : "";

            profileName.setText(username);
            profileEmail.setText(email);
            profileBirthday.setText(birthday);

            Log.d(TAG, "Loaded user data - Name: " + username + ", Email: " + email);
        }
        if (userCursor != null) {
            userCursor.close();
        }
    }
} 