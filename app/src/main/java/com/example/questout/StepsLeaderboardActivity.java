package com.example.questout;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.LayoutInflater;
import android.graphics.Color;
import android.widget.ImageView;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class StepsLeaderboardActivity extends AppCompatActivity implements SensorEventListener {
    private SharedPreferences prefs;
    private LinearLayout leaderboardContainer;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepOffset = -1;
    private int stepsToday = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private TextView yourStepsText;
    private TextView yourRankText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_leaderboard);

        prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
        leaderboardContainer = findViewById(R.id.leaderboardContainer);
        yourStepsText = findViewById(R.id.yourStepsText);
        yourRankText = findViewById(R.id.yourRankText);

        // Back button
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        // Initialize step counter
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepOffset = prefs.getInt("stepOffset", -1);
        stepsToday = prefs.getInt("stepsToday", 0);

        // Add your entry
        addLeaderboardEntry("YOU", stepsToday, 0, true);

        // Update your steps
        updateYourSteps();

        // Start periodic updates
        startPeriodicUpdates();
    }

    private void startPeriodicUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateYourSteps();
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(updateRunnable);
    }

    private void updateYourSteps() {
        if (yourStepsText != null) {
            yourStepsText.setText(stepsToday + " STEPS");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalStepsSinceBoot = (int) event.values[0];
            if (stepOffset == -1) {
                stepOffset = totalStepsSinceBoot;
                prefs.edit().putInt("stepOffset", stepOffset).apply();
            }
            stepsToday = totalStepsSinceBoot - stepOffset;
            if (stepsToday < 0) stepsToday = 0;
            prefs.edit().putInt("stepsToday", stepsToday).apply();
            updateYourSteps();

            // Save steps to database
            int userId = prefs.getInt("userId", -1);
            DBHelper dbHelper = new DBHelper(this);
            dbHelper.updateUserSteps(userId, stepsToday);
            Toast.makeText(this, "Saved to DB: Steps=" + stepsToday, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void addLeaderboardEntry(String name, int steps, int rank, boolean isCurrentUser) {
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

        // Set steps
        stepsText.setText(steps + " STEPS");

        leaderboardContainer.addView(entryView);
    }
} 