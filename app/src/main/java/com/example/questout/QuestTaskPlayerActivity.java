package com.example.questout;

import android.app.AlertDialog;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class QuestTaskPlayerActivity extends AppCompatActivity {
    private TextView taskTitleText;
    private TextView timerText;
    private TextView repsText;
    private TextView taskProgressText;
    private ImageView taskImage;
    private CircularProgressIndicator circularProgress;
    private Button skipButton;
    private Button nextButton;
    private ImageButton backBtn;
    
    private QuestTask currentTask;
    private CountDownTimer timer;
    private boolean isTimerTask = false;
    private int currentTaskIndex = 0;
    private QuestTask[] tasks;
    private boolean allowRewards;
    private int skippedTasksCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_task_player);

        // Get tasks from intent
        Parcelable[] parcelables = getIntent().getParcelableArrayExtra("tasks");
        if (parcelables != null) {
            tasks = new QuestTask[parcelables.length];
            for (int i = 0; i < parcelables.length; i++) {
                tasks[i] = (QuestTask) parcelables[i];
            }
        }

        // Get rewards flag
        allowRewards = getIntent().getBooleanExtra("allow_rewards", true);

        // Initialize views
        taskTitleText = findViewById(R.id.taskTitleText);
        timerText = findViewById(R.id.timerText);
        repsText = findViewById(R.id.setsCounterText);
        taskProgressText = findViewById(R.id.taskProgressText);
        taskImage = findViewById(R.id.taskImage);
        circularProgress = findViewById(R.id.circularProgress);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.resetNextButton);
        backBtn = findViewById(R.id.backBtn);

        // Set initial task
        currentTaskIndex = 0;
        currentTask = tasks[currentTaskIndex];
        showReadyDialog();

        // Setup click listeners
        backBtn.setOnClickListener(v -> onBackPressed());
        skipButton.setOnClickListener(v -> showSkipWarningDialog());
        nextButton.setOnClickListener(v -> {
            if (nextButton.getText().toString().equals("RESET")) {
                showResetDialog();
            } else {
                showCompletionDialog();
            }
        });
    }

    private void showReadyDialog() {
        String message = isTimerTask ? 
            "Ready to start " + currentTask.name + " for " + currentTask.reps + " seconds?" :
            "Ready to start " + currentTask.name + " (" + currentTask.reps + ")?";

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ready, null);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button startButton = dialogView.findViewById(R.id.startButton);
        Button notYetButton = dialogView.findViewById(R.id.notYetButton);

        dialogMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        startButton.setOnClickListener(v -> {
            dialog.dismiss();
            showCountdownDialog(() -> startTask(currentTask));
        });

        notYetButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCompletionDialog() {
        String message = isTimerTask ?
            "Have you completed the " + currentTask.name + " for " + currentTask.reps + " seconds?" :
            "Have you completed " + currentTask.name + " (" + currentTask.reps + ")?";

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_complete, null);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button completedButton = dialogView.findViewById(R.id.completedButton);
        Button notYetButton = dialogView.findViewById(R.id.notYetButton);

        dialogMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        completedButton.setOnClickListener(v -> {
            dialog.dismiss();
            completeTask();
        });

        notYetButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showResetDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reset, null);
        Button resetButton = dialogView.findViewById(R.id.resetButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        resetButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (timer != null) {
                timer.cancel();
            }
            startTask(currentTask);
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSkipWarningDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_skip_warning, null);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button proceedButton = dialogView.findViewById(R.id.proceedButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        proceedButton.setOnClickListener(v -> {
            dialog.dismiss();
            actuallySkipTask();
        });

        dialog.show();
    }

    private void actuallySkipTask() {
        if (timer != null) {
            timer.cancel();
        }
        currentTask.isCompleted = false; // Mark task as not completed when skipped
        skippedTasksCount++; // Increment skipped tasks counter
        moveToNextTask();
    }

    private void showCountdownDialog(Runnable onFinish) {
        final AlertDialog[] dialog = new AlertDialog[1];
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_countdown, null);
        TextView countdownText = dialogView.findViewById(R.id.countdownText);

        dialog[0] = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        dialog[0].show();

        new Thread(() -> {
            try {
                for (int i = 3; i > 0; i--) {
                    final int count = i;
                    runOnUiThread(() -> countdownText.setText(String.valueOf(count)));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                dialog[0].dismiss();
                onFinish.run();
            });
        }).start();
    }

    private void startTask(QuestTask task) {
        taskTitleText.setText(task.name);
        taskImage.setImageResource(task.imageResId);
        updateTaskProgress();
        
        if (task.isTimerTask) {
            startTimerTask(task);
        } else {
            startRepsTask(task);
        }
    }

    private void startTimerTask(QuestTask task) {
        isTimerTask = true;
        // Extract just the number from the string (e.g., "30 seconds" -> 30)
        String durationStr = task.reps.split(" ")[0];
        int duration = Integer.parseInt(durationStr);
        
        // Show timer UI
        timerText.setVisibility(View.VISIBLE);
        repsText.setVisibility(View.GONE);
        circularProgress.setVisibility(View.VISIBLE);
        circularProgress.setMax(duration * 1000);
        circularProgress.setProgress(duration * 1000);
        
        // Start timer
        timer = new CountDownTimer(duration * 1000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timerText.setText(String.format("%02d", seconds));
                circularProgress.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerText.setText("00");
                circularProgress.setProgress(0);
                nextButton.setText("NEXT");
                nextButton.setEnabled(true);
                currentTask.isCompleted = true;
            }
        }.start();
        
        nextButton.setText("RESET");
        nextButton.setEnabled(true);
    }

    private void startRepsTask(QuestTask task) {
        isTimerTask = false;
        repsText.setText(task.reps);
        repsText.setVisibility(View.VISIBLE);
        timerText.setVisibility(View.GONE);
        circularProgress.setVisibility(View.GONE);
        
        nextButton.setText("NEXT");
        nextButton.setEnabled(true);
    }

    private void updateTaskProgress() {
        taskProgressText.setText(String.format("Task %d of %d", currentTaskIndex + 1, tasks.length));
    }

    private void completeTask() {
        if (isTimerTask && timer != null) {
            timer.cancel();
        }
        currentTask.isCompleted = true;
        moveToNextTask();
    }

    private void moveToNextTask() {
        currentTaskIndex++;
        if (currentTaskIndex < tasks.length) {
            currentTask = tasks[currentTaskIndex];
            showReadyDialog();
        } else {
            // All tasks completed or skipped, just return the tasks as is
            Intent resultIntent = new Intent();
            resultIntent.putExtra("completed_tasks", tasks);
            resultIntent.putExtra("allow_rewards", allowRewards);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
} 