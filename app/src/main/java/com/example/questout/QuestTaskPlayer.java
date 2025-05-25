package com.example.questout;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class QuestTaskPlayer extends AppCompatActivity {
    private TextView taskTitleText;
    private TextView timerText;
    private TextView repsText;
    private TextView taskProgressText;
    private ImageView taskImage;
    private Button skipButton;
    private Button nextButton;
    private ImageButton backBtn;
    
    private QuestTask currentTask;
    private CountDownTimer timer;
    private boolean isTimerTask = false;
    private int currentTaskIndex = 0;
    private QuestTask[] tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_task_player);

        // Initialize views
        taskTitleText = findViewById(R.id.taskTitleText);
        timerText = findViewById(R.id.timerText);
        repsText = findViewById(R.id.setsCounterText);
        taskProgressText = findViewById(R.id.taskProgressText);
        taskImage = findViewById(R.id.taskImage);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.resetNextButton);
        backBtn = findViewById(R.id.backBtn);

        // Get tasks from intent
        Parcelable[] parcelables = getIntent().getParcelableArrayExtra("tasks");
        if (parcelables != null) {
            tasks = new QuestTask[parcelables.length];
            for (int i = 0; i < parcelables.length; i++) {
                tasks[i] = (QuestTask) parcelables[i];
            }
            if (tasks.length > 0) {
                currentTask = tasks[0];
                startTask(currentTask);
            }
        }

        // Setup click listeners
        backBtn.setOnClickListener(v -> onBackPressed());
        skipButton.setOnClickListener(v -> skipTask());
        nextButton.setOnClickListener(v -> completeTask());
    }

    private void startTask(QuestTask task) {
        taskTitleText.setText(task.name);
        taskImage.setImageResource(task.imageResId);
        updateTaskProgress();
        
        // Show reps
        repsText.setText(task.reps);
        repsText.setVisibility(View.VISIBLE);
        timerText.setVisibility(View.GONE);
        
        nextButton.setText("NEXT");
        nextButton.setEnabled(true);
    }

    private void updateTaskProgress() {
        taskProgressText.setText(String.format("Task %d of %d", currentTaskIndex + 1, tasks.length));
    }

    private void completeTask() {
        currentTask.isCompleted = true;
        moveToNextTask();
    }

    private void moveToNextTask() {
        currentTaskIndex++;
        if (currentTaskIndex < tasks.length) {
            currentTask = tasks[currentTaskIndex];
            startTask(currentTask);
        } else {
            // All tasks completed
            Intent resultIntent = new Intent();
            resultIntent.putExtra("completed", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void skipTask() {
        moveToNextTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
} 