package com.example.questout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button signUpBtn;
    private TextView backToLoginLink;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        nameInput = findViewById(R.id.name);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirmPassword);
        signUpBtn = findViewById(R.id.signUpBtn);
        backToLoginLink = findViewById(R.id.backToLoginLink);

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Set up click listeners
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                // Validate inputs
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if user already exists
                if (dbHelper.userExists(name, email)) {
                    Toast.makeText(SignupActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create new user
                if (dbHelper.insertUser(name, email, password)) {
                    // Get the new user's ID
                    Cursor cursor = dbHelper.authenticateUser(email, password);
                    if (cursor.moveToFirst()) {
                        int userId = cursor.getInt(0);
                        cursor.close();

                        // Initialize game state in SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("QuestOutPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        
                        // Clear any existing data
                        editor.clear();
                        
                        // Set user session data
                        editor.putInt("userId", userId);
                        editor.putString("userName", name);
                        editor.putString("userEmail", email);
                        
                        // Initialize game state
                        editor.putInt("xp", 0);
                        editor.putInt("level", 0);
                        editor.putInt("streak", 0);
                        editor.putInt("questStreak", 0);
                        editor.putInt("steps", 0);
                        editor.putInt("stepsToday", 0);
                        editor.putInt("goalSteps", 0);
                        editor.putInt("totalQuests", 0);
                        editor.putInt("totalTasks", 0);
                        editor.putInt("highestStreak", 0);
                        editor.putLong("lastLoginDate", 0);
                        editor.putBoolean("isQuestActive", false);
                        editor.apply();

                        // Update database with initial stats
                        dbHelper.updateUserStats(userId, 1, 0, 0);
                        dbHelper.updateTotalXP(userId, 0);
                        
                        Toast.makeText(SignupActivity.this, "Signup successful! Please login.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Signup failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backToLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
} 