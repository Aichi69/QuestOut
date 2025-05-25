package com.example.questout;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import android.content.Intent;
import java.util.Collections;
import java.util.Arrays;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Parcelable;
import android.app.AlertDialog;

public class DailyQuestActivity extends AppCompatActivity {
    private Button startButton;
    private ImageButton backBtn;
    private ProgressBar questProgressBar;
    private boolean isQuestActive = false;
    private long startTime;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private SharedPreferences prefs;
    private RecyclerView exercisesRecyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<QuestTask> selectedExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quest);

        prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
        checkAndResetStreak();

        // Initialize views
        startButton = findViewById(R.id.startButton);
        backBtn = findViewById(R.id.backBtn);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        questProgressBar = findViewById(R.id.questProgressBar);

        // Progress Bar
        questProgressBar.setMax(100);
        questProgressBar.setProgress(0);

        // Setup RecyclerView
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Create and set adapter with initial exercises
        selectedExercises = generateRandomExercises();
        exerciseAdapter = new ExerciseAdapter(selectedExercises);
        exercisesRecyclerView.setAdapter(exerciseAdapter);

        // Setup click listeners
        backBtn.setOnClickListener(v -> onBackPressed());
        startButton.setOnClickListener(v -> {
            if (!isQuestActive) {
                startQuest();
            } else {
                pauseQuest();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset progress
        questProgressBar.setProgress(0);
    }

    private void checkAndResetStreak() {
        long lastQuestDate = prefs.getLong("lastQuestDate", 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        if (lastQuestDate > 0) {
            calendar.setTimeInMillis(lastQuestDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long lastQuestDay = calendar.getTimeInMillis();

            // If more than one day has passed since last quest
            if (today - lastQuestDay > TimeUnit.DAYS.toMillis(1)) {
                int currentStreak = prefs.getInt("questStreak", 0);
                if (currentStreak > 0) {
                    Toast.makeText(this, "Streak reset! You missed a day.", Toast.LENGTH_LONG).show();
                    prefs.edit().putInt("questStreak", 0).apply();
                }
            }
        }
    }

    private List<QuestTask> generateRandomExercises() {
        // Create list of all available exercises
        QuestTask[] allTasks = {
            new QuestTask("Push-ups", "10 reps", R.drawable.pushup, false),
            new QuestTask("Sit-ups", "15 reps", R.drawable.situp, false),
            new QuestTask("Plank", "30 seconds", R.drawable.plank, true),
            new QuestTask("Wall Sit", "30 seconds", R.drawable.wall_sit, true),
            new QuestTask("Squats", "15 reps", R.drawable.squat, false),
            new QuestTask("Tricep Dips", "12 reps", R.drawable.triceps_dips, false)
        };

        // Randomly select 5 exercises
        List<QuestTask> tasks = new ArrayList<>(Arrays.asList(allTasks));
        Collections.shuffle(tasks);
        return tasks.subList(0, 5);
    }

    private void startQuest() {
        // Check if XP has already been claimed today
        long lastQuestDate = prefs.getLong("lastQuestDate", 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        if (lastQuestDate > 0) {
            calendar.setTimeInMillis(lastQuestDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long lastQuestDay = calendar.getTimeInMillis();

            // If quest was completed today
            if (today == lastQuestDay) {
                // Show dialog
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_already_claimed, null);
                Button okButton = dialogView.findViewById(R.id.okButton);
                Button reattemptButton = dialogView.findViewById(R.id.reattemptButton);

                AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

                okButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    startQuestWithRewards(); // Start quest with rewards
                });
                reattemptButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    startQuestWithoutRewards(); // Start quest without rewards
                });

                dialog.show();
                return;
            }
        }

        startQuestWithRewards();
    }

    private void startQuestWithRewards() {
        isQuestActive = true;
        startTime = System.currentTimeMillis();
        startButton.setText("PAUSE");
        
        Intent intent = new Intent(this, QuestTaskPlayerActivity.class);
        intent.putExtra("tasks", selectedExercises.toArray(new QuestTask[0]));
        intent.putExtra("allow_rewards", true);
        startActivityForResult(intent, 1);
        Toast.makeText(this, "Quest started! Good luck!", Toast.LENGTH_SHORT).show();
    }

    private void startQuestWithoutRewards() {
        isQuestActive = true;
        startTime = System.currentTimeMillis();
        startButton.setText("PAUSE");
        
        Intent intent = new Intent(this, QuestTaskPlayerActivity.class);
        intent.putExtra("tasks", selectedExercises.toArray(new QuestTask[0]));
        intent.putExtra("allow_rewards", false);
        startActivityForResult(intent, 1);
        Toast.makeText(this, "Practice mode started! No rewards will be given.", Toast.LENGTH_SHORT).show();
    }

    private void pauseQuest() {
        isQuestActive = false;
        startButton.setText("RESUME");
        handler.removeCallbacks(timerRunnable);
        
        Toast.makeText(this, "Quest paused", Toast.LENGTH_SHORT).show();
    }

    private void updateProgress() {
        int completedTasks = 0;
        int totalTasks = selectedExercises.size();
        
        for (QuestTask task : selectedExercises) {
            if (task.isCompleted) completedTasks++;
        }
        
        int progress = (completedTasks * 100) / totalTasks;
        questProgressBar.setProgress(progress);
        
        if (progress == 100) {
            // Calculate rewards
            int baseXP = 100;
            int currentStreak = prefs.getInt("questStreak", 0);
            int newStreak = currentStreak + 1;
            int streakBonus = (newStreak / 7) * 50; // Extra 50 XP for each week of streak
            int totalXP = baseXP + streakBonus;

            // Update streak
            prefs.edit()
                .putInt("questStreak", newStreak)
                .putLong("lastQuestDate", System.currentTimeMillis())
                .apply();

            // Update streak and lastQuestDate in database
            int userId = prefs.getInt("userId", -1);
            DBHelper dbHelper = new DBHelper(this);
            dbHelper.updateUserStreak(userId, newStreak);
            dbHelper.incrementTotalQuests(userId);
            Toast.makeText(this, "Saved to DB: Streak=" + newStreak, Toast.LENGTH_SHORT).show();

            // Start QuestCompleteActivity
            Intent intent = new Intent(DailyQuestActivity.this, QuestCompleteActivity.class);
            intent.putExtra("total_xp", totalXP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Parcelable[] parcelables = data.getParcelableArrayExtra("completed_tasks");
            boolean allowRewards = data.getBooleanExtra("allow_rewards", true);
            
            if (parcelables != null) {
                QuestTask[] completedTasks = new QuestTask[parcelables.length];
                for (int i = 0; i < parcelables.length; i++) {
                    completedTasks[i] = (QuestTask) parcelables[i];
                }
                
                // Update exercises completion status
                for (QuestTask completedTask : completedTasks) {
                    for (QuestTask task : selectedExercises) {
                        if (task.name.equals(completedTask.name)) {
                            task.isCompleted = completedTask.isCompleted;
                            break;
                        }
                    }
                }
                exerciseAdapter.notifyDataSetChanged();
                
                // Always proceed to completion screen after the last task
                if (allowRewards) {
                    // Calculate XP: 10 XP per completed task
                    int completedCount = 0;
                    for (QuestTask task : selectedExercises) {
                        if (task.isCompleted) completedCount++;
                    }
                    int totalXP = completedCount * 10;

                    // Start QuestCompleteActivity
                    Intent intent = new Intent(DailyQuestActivity.this, QuestCompleteActivity.class);
                    intent.putExtra("total_xp", totalXP);
                    startActivity(intent);
                    finish();
                } else {
                    // Just show completion message for practice mode
                    Toast.makeText(this, "Practice quest completed! No rewards given.", Toast.LENGTH_LONG).show();
                    isQuestActive = false;
                    startButton.setText("START");
                }
            }
        } else {
            // Quest was cancelled or failed
            isQuestActive = false;
            startButton.setText("START");
        }
    }
} 