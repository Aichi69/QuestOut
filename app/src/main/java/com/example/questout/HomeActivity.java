package com.example.questout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.app.Dialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import java.util.Calendar;
import android.database.Cursor;
import androidx.annotation.Nullable;

public class HomeActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private int xp, streak, level;
    private int totalSteps = 0, goalSteps = 5000;
    private int questStreak = 0;
    private SensorManager sensorManager;
    private TextView profileLevel, stepsProgress, stepsGoal, questStreakText, profileName;
    private Button claimLoginBtn, claimStepsBtn, dailyQuestBtn, questLeaderboardBtn, stepsLeaderboardBtn, setGoalBtn;
    private LinearLayout navHome, navProfile, navAchievement;
    private ImageView navHomeIcon, navProfileIcon, navAchievementIcon;
    private TextView navHomeText, navProfileText, navAchievementText;
    private ProgressBar stepsProgressBar;
    private int stepOffset = -1; // The reference value for local step counting
    private int stepsToday = 0;
    private ImageView[] dayIcons; // Make dayIcons accessible throughout the class
    private DBHelper dbHelper;
    private static final String TAG = "HomeActivity";
    private static final int PROFILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize DBHelper
        dbHelper = new DBHelper(this);
        prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);

        // Initialize UI elements first
        initializeUI();

        // Get current user ID
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e(TAG, "No user ID found in preferences!");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Load user data from database
        loadUserData(userId);
        
        // Start step counter
        startStepCounter();
    }

    private void initializeUI() {
        // Initialize UI elements
        profileLevel = findViewById(R.id.profileLevel);
        profileName = findViewById(R.id.profileName);
        questStreakText = findViewById(R.id.questStreakText);
        stepsProgress = findViewById(R.id.stepsProgress);
        stepsGoal = findViewById(R.id.stepsGoal);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);
        claimStepsBtn = findViewById(R.id.claimStepsBtn);
        claimLoginBtn = findViewById(R.id.claimLoginBtn);
        dailyQuestBtn = findViewById(R.id.dailyQuestBtn);
        questLeaderboardBtn = findViewById(R.id.questLeaderboardBtn);
        stepsLeaderboardBtn = findViewById(R.id.stepsLeaderboardBtn);
        setGoalBtn = findViewById(R.id.setGoalBtn);

        // Initialize streak indicators
        ImageView monIcon = findViewById(R.id.monIcon);
        ImageView tueIcon = findViewById(R.id.tueIcon);
        ImageView wedIcon = findViewById(R.id.wedIcon);
        ImageView thuIcon = findViewById(R.id.thuIcon);
        ImageView friIcon = findViewById(R.id.friIcon);
        ImageView satIcon = findViewById(R.id.satIcon);
        ImageView sunIcon = findViewById(R.id.sunIcon);
        dayIcons = new ImageView[]{monIcon, tueIcon, wedIcon, thuIcon, friIcon, satIcon, sunIcon};

        // Set initial values
        if (profileLevel != null) {
            profileLevel.setText("0");
        }
        if (questStreakText != null) questStreakText.setText("0");
        if (stepsProgress != null) stepsProgress.setText("0");
        if (stepsGoal != null) stepsGoal.setText("Set a goal to start tracking steps");
        if (stepsProgressBar != null) stepsProgressBar.setProgress(0);

        // Initialize navigation
        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);
        navAchievement = findViewById(R.id.navAchievement);
        navHomeIcon = findViewById(R.id.navHomeIcon);
        navProfileIcon = findViewById(R.id.navProfileIcon);
        navAchievementIcon = findViewById(R.id.navAchievementIcon);
        if (navHome != null) navHomeText = (TextView) navHome.getChildAt(1);
        if (navProfile != null) navProfileText = (TextView) navProfile.getChildAt(1);
        if (navAchievement != null) navAchievementText = (TextView) navAchievement.getChildAt(1);

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        if (claimLoginBtn != null) {
            claimLoginBtn.setOnClickListener(v -> handleLoginClaim());
        }
        if (claimStepsBtn != null) {
            claimStepsBtn.setOnClickListener(v -> handleStepsClaim());
        }
        if (dailyQuestBtn != null) {
            dailyQuestBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, DailyQuestActivity.class));
            });
        }
        if (questLeaderboardBtn != null) {
            questLeaderboardBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, QuestLeaderboardActivity.class));
            });
        }
        if (stepsLeaderboardBtn != null) {
            stepsLeaderboardBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, StepsLeaderboardActivity.class));
            });
        }
        if (setGoalBtn != null) {
            setGoalBtn.setOnClickListener(v -> showSetGoalDialog());
        }
        if (navHome != null) {
            navHome.setOnClickListener(v -> highlightNavTab("home"));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                highlightNavTab("profile");
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }
        if (navAchievement != null) {
            navAchievement.setOnClickListener(v -> {
                highlightNavTab("achievement");
                startActivity(new Intent(this, AchievementActivity.class));
            });
        }
    }

    private void highlightNavTab(String tab) {
        // Reset all
        navHomeIcon.setColorFilter(getResources().getColor(R.color.inactiveColor));
        navProfileIcon.setColorFilter(getResources().getColor(R.color.inactiveColor));
        navAchievementIcon.setColorFilter(getResources().getColor(R.color.inactiveColor));
        navHomeText.setTextColor(getResources().getColor(R.color.inactiveColor));
        navProfileText.setTextColor(getResources().getColor(R.color.inactiveColor));
        navAchievementText.setTextColor(getResources().getColor(R.color.inactiveColor));
        // Highlight selected
        if (tab.equals("home")) {
            navHomeIcon.setColorFilter(getResources().getColor(R.color.activeColor));
            navHomeText.setTextColor(getResources().getColor(R.color.activeColor));
        } else if (tab.equals("profile")) {
            navProfileIcon.setColorFilter(getResources().getColor(R.color.activeColor));
            navProfileText.setTextColor(getResources().getColor(R.color.activeColor));
        } else if (tab.equals("achievement")) {
            navAchievementIcon.setColorFilter(getResources().getColor(R.color.activeColor));
            navAchievementText.setTextColor(getResources().getColor(R.color.activeColor));
        }
    }

    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d("StepDebug", "Sensor changed: " + event.values[0]);
            int totalStepsSinceBoot = (int) event.values[0];
            int userId = prefs.getInt("userId", -1);
            
            if (stepOffset == -1) {
                // Try to load step offset from SharedPreferences first
                stepOffset = prefs.getInt("stepOffset", -1);
                
                // If still not found, use current boot count as offset
                if (stepOffset == -1) {
                    stepOffset = totalStepsSinceBoot;
                }
                
                // Save to SharedPreferences and database for persistence
                prefs.edit().putInt("stepOffset", stepOffset).apply();
                if (userId != -1) {
                    dbHelper.saveStepOffset(userId, stepOffset);
                }
            }
            
            // Calculate steps today based on offset
            stepsToday = totalStepsSinceBoot - stepOffset;
            if (stepsToday < 0) stepsToday = 0;
            
            // Save to SharedPreferences
            prefs.edit().putInt("stepsToday", stepsToday).apply();
            
            // Update UI and database
            updateSteps(stepsToday);
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepListener);
        }
    }

    private void updateStepsProgress() {
        if (stepsProgress != null && stepsGoal != null && stepsProgressBar != null) {
            stepsProgress.setText(String.valueOf(stepsToday));
            stepsProgressBar.setMax(goalSteps);
            stepsProgressBar.setProgress(stepsToday);
        }
    }

    // Method to update steps (call this when steps change)
    public void updateSteps(int newSteps) {
        stepsToday = newSteps;
        updateStepsProgress();
        
        // Save steps to database for leaderboard
        int userId = prefs.getInt("userId", -1);
        if (userId != -1) {
            // Update both steps and stepsToday in the database
            dbHelper.updateUserSteps(userId, stepsToday);
            dbHelper.updateStepsToday(userId, stepsToday);
            Log.d(TAG, "Updated steps and stepsToday in database: " + stepsToday);
        }
    }

    // Method to update goal (call this when goal changes)
    public void updateGoal(int newGoal) {
        goalSteps = newGoal;
        
        // Save to database
        int userId = prefs.getInt("userId", -1);
        if (userId != -1) {
            dbHelper.updateStepGoal(userId, newGoal);
        }
        
        // Save to SharedPreferences
        prefs.edit()
            .putInt("goalSteps", goalSteps)
            .putLong("lastGoalSetTime", System.currentTimeMillis())
            .apply();
            
        updateStepsProgress();
        Log.d(TAG, "Updated step goal to " + newGoal);
    }

    private void updateLevel(int newXp) {
        int newLevel = newXp / 50;
        if (newLevel > level) {
            // Show level up dialog
            LevelUpDialog dialog = new LevelUpDialog(this, newLevel, () -> {
                Toast.makeText(this, "Welcome to level " + newLevel + "!", Toast.LENGTH_SHORT).show();
            });
            dialog.show();
        }
        
        // Update database
        int userId = prefs.getInt("userId", -1);
        if (userId != -1) {
            dbHelper.updateTotalXP(userId, newXp);
            dbHelper.updateUserLevel(userId, newLevel);
            
            // Update UI
            level = newLevel;
            xp = newXp;
            
            // Update all level displays
            if (profileLevel != null) {
                profileLevel.setText(String.valueOf(level));
            }
            
            // Save to SharedPreferences
            prefs.edit()
                .putInt("level", level)
                .putInt("xp", newXp)
                .apply();
            
            Log.d(TAG, "Updated level to " + newLevel + " with " + newXp + " XP");
        }
    }

    private void showSetGoalDialog() {
        // Check if goal was already set today
        long lastGoalSetTime = prefs.getLong("lastGoalSetTime", 0);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (lastGoalSetTime > today.getTimeInMillis()) {
            // Goal already set today
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Goal Already Set")
                .setMessage("You can only set your step goal once per day. Come back tomorrow!")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_set_goal);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        EditText goalInput = dialog.findViewById(R.id.goalInput);
        Button saveGoalBtn = dialog.findViewById(R.id.saveGoalBtn);
        Button cancelGoalBtn = dialog.findViewById(R.id.cancelGoalBtn);
        TextView xpInfoText = dialog.findViewById(R.id.xpInfoText);

        // Set XP information
        String xpInfo = "Step Goal Rewards:\n" +
                       "• 2,500 steps = 5 XP\n" +
                       "• 5,000 steps = 10 XP\n" +
                       "• 7,500 steps = 15 XP\n" +
                       "• 10,000+ steps = 20 XP";
        xpInfoText.setText(xpInfo);

        // Pre-fill with current goal if exists
        goalInput.setText(String.valueOf(goalSteps));

        saveGoalBtn.setOnClickListener(v -> {
            String input = goalInput.getText().toString().trim();
            if (!input.isEmpty()) {
                int newGoal = Integer.parseInt(input);
                if (newGoal >= 2500) {
                    updateGoal(newGoal);
                    dialog.dismiss();
                } else {
                    goalInput.setError("Minimum goal is 2,500 steps");
                }
            } else {
                goalInput.setError("Please enter a goal");
            }
        });
        cancelGoalBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void checkActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        1001);
            }
        }
    }

    private void startStepCounter() {
        // Check permission first
        checkActivityRecognitionPermission();

        // Step counter logic (local sensor, persistent)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) {
            Toast.makeText(this, "Step Counter Sensor not available!", Toast.LENGTH_LONG).show();
        } else {
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the step counter
                Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                if (stepSensor != null) {
                    startStepCounter();
                }
            } else {
                Toast.makeText(this, "Activity Recognition permission is required for step counting.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Add new method to update quest streak display
    private void updateQuestStreakDisplay() {
        int questStreak = prefs.getInt("questStreak", 0);
        questStreakText.setText("Daily Streak: " + questStreak);
    }

    // Add method to increment quest streak
    public void incrementQuestStreak() {
        int questStreak = prefs.getInt("questStreak", 0);
        questStreak++;
        prefs.edit().putInt("questStreak", questStreak).apply();
        updateQuestStreakDisplay();
        
        // Award XP for completing quest
        int streakXp = 50; // Base XP for completing quest
        if (questStreak > 1) {
            streakXp += (questStreak - 1) * 10; // Bonus XP for streak
        }
        
        int newXp = xp + streakXp;
        prefs.edit().putInt("xp", newXp).apply();
        updateLevel(newXp);
        Toast.makeText(this, "Quest completed! +" + streakXp + " XP!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save current stats to database
        int userId = prefs.getInt("userId", -1);
        if (userId != -1) {
            dbHelper.updateUserSteps(userId, stepsToday);
            dbHelper.updateUserStreak(userId, streak);
            dbHelper.updateTotalXP(userId, xp);
            dbHelper.updateUserLevel(userId, level);
            dbHelper.updateStepGoal(userId, goalSteps);
            Log.d(TAG, "Saved user stats to database - Level: " + level + ", Steps: " + stepsToday + ", Goal: " + goalSteps);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Load latest data from database
        int userId = prefs.getInt("userId", -1);
        if (userId != -1) {
            loadUserData(userId);
            
            // Check if daily login has been claimed today
            long lastLoginDate = prefs.getLong("lastLoginDate", 0);
            Calendar today = Calendar.getInstance();
            Calendar lastLogin = Calendar.getInstance();
            lastLogin.setTimeInMillis(lastLoginDate);
            
            boolean isSameDay = today.get(Calendar.YEAR) == lastLogin.get(Calendar.YEAR) &&
                              today.get(Calendar.DAY_OF_YEAR) == lastLogin.get(Calendar.DAY_OF_YEAR);
            
            if (isSameDay) {
                claimLoginBtn.setEnabled(false);
                claimLoginBtn.setText("CLAIMED");
                claimLoginBtn.setAlpha(0.5f);
            } else {
                claimLoginBtn.setEnabled(true);
                claimLoginBtn.setText("CLAIM");
                claimLoginBtn.setAlpha(1.0f);
            }
            
            // Update steps progress
            updateStepsProgress();
        }
    }

    private void loadUserData(int userId) {
        Cursor userCursor = dbHelper.getUserById(userId);
        if (userCursor != null && userCursor.moveToFirst()) {
            int nameIndex = userCursor.getColumnIndex("name");
            int levelIndex = userCursor.getColumnIndex("level");
            int stepsIndex = userCursor.getColumnIndex("steps");
            int stepsToday_Index = userCursor.getColumnIndex("stepsToday");
            int streakIndex = userCursor.getColumnIndex("streak");
            int questStreakIndex = userCursor.getColumnIndex("questStreak");
            int xpIndex = userCursor.getColumnIndex("total_xp");
            int goalIndex = userCursor.getColumnIndex("stepGoal");
            int stepOffsetIndex = userCursor.getColumnIndex("stepOffset");

            String username = nameIndex >= 0 ? userCursor.getString(nameIndex) : "Player";
            level = levelIndex >= 0 ? userCursor.getInt(levelIndex) : 0;
            int steps = stepsIndex >= 0 ? userCursor.getInt(stepsIndex) : 0;
            int dbStepsToday = stepsToday_Index >= 0 ? userCursor.getInt(stepsToday_Index) : 0;
            streak = streakIndex >= 0 ? userCursor.getInt(streakIndex) : 0;
            questStreak = questStreakIndex >= 0 ? userCursor.getInt(questStreakIndex) : 0;
            xp = xpIndex >= 0 ? userCursor.getInt(xpIndex) : 0;
            goalSteps = goalIndex >= 0 ? userCursor.getInt(goalIndex) : 5000; // Default to 5000 if not set
            
            // Load step offset from database if available
            try {
                if (stepOffsetIndex >= 0) {
                    int dbStepOffset = userCursor.getInt(stepOffsetIndex);
                    if (dbStepOffset > 0) {
                        // Only update if we have a valid offset from the database
                        stepOffset = dbStepOffset;
                        prefs.edit().putInt("stepOffset", stepOffset).apply();
                        Log.d(TAG, "Loaded step offset from database: " + stepOffset);
                    } else {
                        // If no valid offset in database, try to get from SharedPreferences
                        stepOffset = prefs.getInt("stepOffset", -1);
                        Log.d(TAG, "Using step offset from SharedPreferences: " + stepOffset);
                    }
                }
            } catch (Exception e) {
                // Handle case where stepOffset column might not exist yet
                Log.e(TAG, "Error accessing stepOffset column: " + e.getMessage());
                // Try to get from SharedPreferences as fallback
                stepOffset = prefs.getInt("stepOffset", -1);
                Log.d(TAG, "Fallback to SharedPreferences for stepOffset: " + stepOffset);
            }
            
            // If we have a valid stepsToday value from the database, use it
            // Prioritize stepsToday over steps for better accuracy
            if (dbStepsToday > 0) {
                stepsToday = dbStepsToday;
                steps = dbStepsToday; // Make sure steps and stepsToday are in sync
                prefs.edit()
                    .putInt("stepsToday", stepsToday)
                    .putInt("steps", steps)
                    .apply();
                Log.d(TAG, "Loaded stepsToday from database: " + stepsToday);
            } else if (steps > 0) {
                // If stepsToday is not available but steps is, use steps
                stepsToday = steps;
                prefs.edit()
                    .putInt("stepsToday", stepsToday)
                    .apply();
                Log.d(TAG, "Using steps value for stepsToday: " + steps);
            }
            
            // Make sure the steps value is properly saved to the database
            if (steps > 0 || stepsToday > 0) {
                // Use the higher value between steps and stepsToday
                int finalSteps = Math.max(steps, stepsToday);
                dbHelper.updateUserSteps(userId, finalSteps);
                dbHelper.updateStepsToday(userId, finalSteps);
                Log.d(TAG, "Updated database with steps: " + finalSteps);
            }
            
            Log.d(TAG, String.format("Loaded user data - Name: %s, Level: %d, Steps: %d, StepsToday: %d, Streak: %d, Quest Streak: %d, XP: %d, Goal: %d",
                username, level, steps, stepsToday, streak, questStreak, xp, goalSteps));

            // Update UI with user data
            updateUIWithUserData(username, level, steps, streak, xp);
            
            // Update step goal display
            if (stepsGoal != null) {
                stepsGoal.setText(goalSteps + " steps");
            }
            
            // Update steps progress
            updateStepsProgress();
        }
        if (userCursor != null) {
            userCursor.close();
        }
    }

    private void updateUIWithUserData(String username, int level, int steps, int streak, int xp) {
        // Update profile level - ensure it shows 0 for new users
        if (profileLevel != null) {
            profileLevel.setText(String.valueOf(level));
        }

        // Update profile name
        profileName.setText("Hello " + username + "!");

        // Update steps - use the value from database if available
        if (steps > 0) {
            stepsToday = steps;
            // Don't call updateSteps here as it would overwrite the database value
            // Just update the UI
            updateStepsProgress();
        }

        // Update streak display
        updateStreakDisplay(streak, dayIcons);

        // Update quest streak text
        if (questStreakText != null) {
            questStreakText.setText("Daily Streak: " + questStreak);
        }

        // Save to SharedPreferences for consistency
        prefs.edit()
            .putInt("level", level)
            .putInt("xp", xp)
            .putInt("streak", streak)
            .putInt("questStreak", questStreak)
            .putInt("steps", steps)         // Save steps to SharedPreferences
            .putInt("stepsToday", steps)    // Save stepsToday to SharedPreferences
            .apply();
            
        Log.d(TAG, "Saved user data to SharedPreferences - Steps: " + steps + ", StepsToday: " + steps);
    }

    // Example methods to show/hide the ongoing quest UI
    private void showOngoingQuestUI() {
        View ongoingQuestView = findViewById(R.id.ongoingQuestContainer);
        if (ongoingQuestView != null) ongoingQuestView.setVisibility(View.VISIBLE);
        // Optionally update text/details
    }

    private void hideOngoingQuestUI() {
        View ongoingQuestView = findViewById(R.id.ongoingQuestContainer);
        if (ongoingQuestView != null) ongoingQuestView.setVisibility(View.GONE);
    }

    // Helper method to update streak display
    private void updateStreakDisplay(int streak, ImageView[] dayIcons) {
        // Show check for the current streak day
        for (int i = 0; i < 7; i++) {
            if (i == streak - 1) {
                dayIcons[i].setImageResource(R.drawable.ic_check_circle);
            } else {
                dayIcons[i].setImageResource(R.drawable.ic_circle);
            }
        }
    }

    private void handleLoginClaim() {
        if (!claimLoginBtn.isEnabled()) {
            // Show dialog: already claimed
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Already Claimed")
                .setMessage("You have already claimed your daily login reward today. Come back tomorrow!")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        int prevXp = prefs.getInt("xp", 0);
        int prevLevel = prefs.getInt("level", 0);
        int newXp = prevXp + 10;
        
        // Get current day of week (0 = Monday, 6 = Sunday)
        Calendar claimCalendar = Calendar.getInstance();
        int dayOfWeek = claimCalendar.get(Calendar.DAY_OF_WEEK);
        int currentDayIndex = (dayOfWeek + 5) % 7;
        
        // Set streak to current day only
        int newStreak = currentDayIndex + 1;
        
        int newLevel = newXp / 50;
        long currentTime = System.currentTimeMillis();
        
        // Save to database first
        int loggedInUserId = prefs.getInt("userId", -1);
        if (loggedInUserId != -1) {
            dbHelper.updateTotalXP(loggedInUserId, newXp);
            dbHelper.updateUserStreak(loggedInUserId, newStreak);
            dbHelper.updateUserLevel(loggedInUserId, newLevel);
            
            // Then update SharedPreferences
            prefs.edit()
                .putInt("xp", newXp)
                .putInt("streak", newStreak)
                .putInt("level", newLevel)
                .putLong("lastLoginDate", currentTime)
                .putInt("lastWeek", claimCalendar.get(Calendar.WEEK_OF_YEAR))
                .apply();
            
            xp = newXp;
            updateLevel(newXp);
            if (profileLevel != null) {
                profileLevel.setText(String.valueOf(newLevel));
            }
            
            // Update streak display
            updateStreakDisplay(newStreak, dayIcons);
            
            claimLoginBtn.setEnabled(false);
            claimLoginBtn.setText("CLAIMED");
            claimLoginBtn.setAlpha(0.5f); // Fade button when claimed

            // Show custom dialog for claim confirmation
            Dialog claimDialog = new Dialog(this);
            claimDialog.setContentView(R.layout.dialog_claim_success);
            claimDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView dialogTitle = claimDialog.findViewById(R.id.dialogTitle);
            TextView dialogMessage = claimDialog.findViewById(R.id.dialogMessage);
            Button dialogOkBtn = claimDialog.findViewById(R.id.dialogOkBtn);
            if (dialogTitle != null) dialogTitle.setText("Daily Login Claimed!");
            if (dialogMessage != null) dialogMessage.setText("+10 XP!");
            dialogOkBtn.setOnClickListener(v2 -> claimDialog.dismiss());
            claimDialog.show();
        }
    }

    private void handleStepsClaim() {
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) return;

        int prevXp = prefs.getInt("xp", 0);
        int stepsToday = prefs.getInt("stepsToday", 0);
        
        // Calculate XP based on steps achieved
        int xpReward = 0;
        if (stepsToday >= 10000) {
            xpReward = 20;
        } else if (stepsToday >= 7500) {
            xpReward = 15;
        } else if (stepsToday >= 5000) {
            xpReward = 10;
        } else if (stepsToday >= 2500) {
            xpReward = 5;
        }

        int newXp = prevXp + xpReward;
        int newLevel = newXp / 50;

        // Update database
        dbHelper.updateTotalXP(userId, newXp);
        dbHelper.updateUserSteps(userId, stepsToday);
        dbHelper.updateUserLevel(userId, newLevel);

        // Update SharedPreferences
        prefs.edit()
            .putInt("xp", newXp)
            .putInt("level", newLevel)
            .apply();

        // Update UI
        xp = newXp;
        updateLevel(newXp);
        Toast.makeText(this, "Steps goal achieved! +" + xpReward + " XP!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the profile name from SharedPreferences
            String userName = prefs.getString("userName", "");
            if (!userName.isEmpty()) {
                profileName.setText(userName);
            }
        }
    }
} 