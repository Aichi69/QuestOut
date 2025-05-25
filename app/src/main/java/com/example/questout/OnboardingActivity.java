package com.example.questout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button getStartedBtn = findViewById(R.id.getStartedBtn);
        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Optionally set a flag if you want onboarding to show only once
                SharedPreferences prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("hasCompletedOnboarding", true).apply();

                // Go to login page
                startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
} 