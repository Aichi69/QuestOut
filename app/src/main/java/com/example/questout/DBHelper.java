package com.example.questout;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "questout.db";
    private static final int DB_VERSION = 5; // Increment version to trigger onUpgrade
    private static final String TAG = "DBHelper";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL UNIQUE," +
            "email TEXT NOT NULL UNIQUE," +
            "password TEXT NOT NULL," +
            "birthday TEXT," +
            "level INTEGER DEFAULT 0," +
            "streak INTEGER DEFAULT 0," +
            "questStreak INTEGER DEFAULT 0," +
            "steps INTEGER DEFAULT 0," +
            "stepsToday INTEGER DEFAULT 0," +
            "stepGoal INTEGER DEFAULT 5000," +
            "stepOffset INTEGER DEFAULT -1," +
            "total_xp INTEGER DEFAULT 0," +
            "total_quests INTEGER DEFAULT 0," +
            "total_tasks INTEGER DEFAULT 0," +
            "highest_streak INTEGER DEFAULT 0," +
            "lastLoginDate INTEGER DEFAULT 0," +
            "isQuestActive INTEGER DEFAULT 0," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // Add columns for version upgrades without dropping the table
        if (oldVersion < 4) {
            // Add birthday column if upgrading from version < 4
            try {
                db.execSQL("ALTER TABLE users ADD COLUMN birthday TEXT");
                Log.d(TAG, "Added birthday column to users table");
            } catch (Exception e) {
                Log.e(TAG, "Error adding birthday column: " + e.getMessage());
            }
        }
        
        // Add stepOffset column for step tracking persistence
        try {
            db.execSQL("ALTER TABLE users ADD COLUMN stepOffset INTEGER DEFAULT -1");
            Log.d(TAG, "Added stepOffset column to users table");
        } catch (Exception e) {
            Log.e(TAG, "Error adding stepOffset column: " + e.getMessage());
            // Column might already exist, which is fine
        }
        
        // We no longer drop the table to preserve user data
        // Instead, we add columns as needed
    }

    // Insert new user
    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("level", 0);  // Start at level 0
        values.put("streak", 0);
        values.put("steps", 0);
        values.put("total_xp", 0);
        values.put("total_quests", 0);
        values.put("total_tasks", 0);
        values.put("highest_streak", 0);
        values.put("stepGoal", 0);
        long result = db.insert("users", null, values);
        Log.d(TAG, "Inserting new user: " + name + " with level 0. Result: " + (result != -1));
        return result != -1;
    }

    // Check if user exists by name or email
    public boolean userExists(String name, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE name=? OR email=?", new String[]{name, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Authenticate user by name/email and password
    public Cursor authenticateUser(String nameOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE (name=? OR email=?) AND password=?", 
            new String[]{nameOrEmail, nameOrEmail, password});
    }

    // Get user by ID with all stats
    public Cursor getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE id=?", new String[]{String.valueOf(id)});
        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, String.format("Retrieved user data - ID: %d, Level: %d", 
                id, cursor.getInt(cursor.getColumnIndex("level"))));
        }
        return cursor;
    }

    // Get leaderboard by steps
    public Cursor getLeaderboardBySteps() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users ORDER BY steps DESC LIMIT 10", null);
    }

    // Get leaderboard by streak
    public Cursor getLeaderboardByStreak() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users ORDER BY streak DESC LIMIT 10", null);
    }

    // Get user level
    public int getUserLevel(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT level FROM users WHERE id = ?", 
            new String[]{String.valueOf(userId)});
        int level = 0; // Default level is now 0
        if (cursor != null && cursor.moveToFirst()) {
            level = cursor.getInt(0);
            Log.d(TAG, "Retrieved level " + level + " for user " + userId);
        }
        if (cursor != null) {
            cursor.close();
        }
        return level;
    }

    // Update user level
    public void updateUserLevel(int userId, int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("level", level);
        int result = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Updated level for user " + userId + " to " + level + ". Result: " + result);
        
        // Verify the update
        Cursor cursor = db.rawQuery("SELECT level FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            int updatedLevel = cursor.getInt(0);
            Log.d(TAG, "Verified level update - Current value: " + updatedLevel);
            cursor.close();
        }
    }

    // Update user stats
    public void updateUserStats(int userId, int level, int xp, int steps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("level", level);
        values.put("total_xp", xp);
        values.put("steps", steps);
        db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
        Log.d(TAG, String.format("Updated user stats - Level: %d, XP: %d, Steps: %d", level, xp, steps));
    }

    // Update user steps
    public void updateUserSteps(int userId, int steps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        int result = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Updated steps for user " + userId + " to " + steps + ". Result: " + result);
        
        // Verify the update
        Cursor cursor = db.rawQuery("SELECT steps FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            int updatedSteps = cursor.getInt(0);
            Log.d(TAG, "Verified steps update - Current value: " + updatedSteps);
            cursor.close();
        }
    }
    
    // Update user steps today (for leaderboard)
    public void updateStepsToday(int userId, int stepsToday) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stepsToday", stepsToday);
        int result = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Updated stepsToday for user " + userId + " to " + stepsToday + ". Result: " + result);
        
        // Verify the update
        Cursor cursor = db.rawQuery("SELECT stepsToday FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            int updatedSteps = cursor.getInt(0);
            Log.d(TAG, "Verified stepsToday update - Current value: " + updatedSteps);
            cursor.close();
        }
    }
    
    // Save step offset for persistent step tracking across sessions
    public void saveStepOffset(int userId, int stepOffset) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stepOffset", stepOffset);
        int result = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Saved step offset for user " + userId + " to " + stepOffset + ". Result: " + result);
    }
    
    // Get step offset for persistent step tracking across sessions
    public int getStepOffset(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT stepOffset FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        int stepOffset = -1;
        if (cursor != null && cursor.moveToFirst()) {
            stepOffset = cursor.getInt(0);
            cursor.close();
        }
        return stepOffset;
    }

    // Update user streak
    public void updateUserStreak(int userId, int streak) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("streak", streak);
        db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Updated streak to " + streak + " for user " + userId);
    }

    // Update quest streak
    public void updateQuestStreak(int userId, int questStreak) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("questStreak", questStreak);
        db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Updated quest streak to " + questStreak + " for user " + userId);
    }

    // Update total XP
    public void updateTotalXP(int userId, int xp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("total_xp", xp);
        db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
    }

    // Update total quests completed
    public void incrementTotalQuests(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE users SET total_quests = total_quests + 1 WHERE id = ?", 
            new String[]{String.valueOf(userId)});
    }

    // Update total tasks completed
    public void incrementTotalTasks(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE users SET total_tasks = total_tasks + 1 WHERE id = ?", 
            new String[]{String.valueOf(userId)});
    }

    // Get highest streak
    public int getHighestStreak(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT highest_streak FROM users WHERE id = ?", 
            new String[]{String.valueOf(userId)});
        int highestStreak = 0;
        if (cursor.moveToFirst()) {
            highestStreak = cursor.getInt(0);
        }
        cursor.close();
        return highestStreak;
    }

    // Delete user account
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("users", "id = ?", new String[]{String.valueOf(userId)});
        return result > 0;
    }

    // Get user statistics
    public Cursor getUserStats(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT level, streak, steps, total_xp, total_quests, total_tasks, highest_streak FROM users WHERE id = ?", 
            new String[]{String.valueOf(userId)});
    }

    // Reset user progress (keep account but reset stats)
    public void resetUserProgress(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("level", 0);
        values.put("streak", 0);
        values.put("steps", 0);
        values.put("total_xp", 0);
        values.put("total_quests", 0);
        values.put("total_tasks", 0);
        values.put("highest_streak", 0);
        values.put("stepGoal", 0);
        db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
    }

    public Cursor getStepsLeaderboard() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id, name, steps, " +
            "RANK() OVER (ORDER BY steps DESC) as rank " +
            "FROM users " +
            "WHERE steps > 0 " +  // Only show users with steps
            "ORDER BY steps DESC",
            null
        );
        if (cursor != null) {
            Log.d(TAG, "Steps leaderboard query returned " + cursor.getCount() + " rows");
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int steps = cursor.getInt(cursor.getColumnIndex("steps"));
                int rank = cursor.getInt(cursor.getColumnIndex("rank"));
                Log.d(TAG, String.format("Steps Leaderboard - Rank %d: %s with %d steps", 
                    rank, name, steps));
            }
            cursor.moveToFirst(); // Reset cursor position
        }
        return cursor;
    }

    public Cursor getQuestLeaderboard() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id, name, streak, " +
            "RANK() OVER (ORDER BY streak DESC) as rank " +
            "FROM users " +
            "WHERE streak > 0 " +  // Only show users with streak
            "ORDER BY streak DESC",
            null
        );
        if (cursor != null) {
            Log.d(TAG, "Quest leaderboard query returned " + cursor.getCount() + " rows");
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int streak = cursor.getInt(cursor.getColumnIndex("streak"));
                int rank = cursor.getInt(cursor.getColumnIndex("rank"));
                Log.d(TAG, String.format("Quest Leaderboard - Rank %d: %s with %d streak", 
                    rank, name, streak));
            }
            cursor.moveToFirst(); // Reset cursor position
        }
        return cursor;
    }

    // Add method to get all users for debugging
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id, name, steps, streak, " +
            "RANK() OVER (ORDER BY steps DESC) as steps_rank, " +
            "RANK() OVER (ORDER BY streak DESC) as streak_rank " +
            "FROM users",
            null
        );
        if (cursor != null) {
            Log.d("DBHelper", "Total users in database: " + cursor.getCount());
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int steps = cursor.getInt(cursor.getColumnIndex("steps"));
                int streak = cursor.getInt(cursor.getColumnIndex("streak"));
                int stepsRank = cursor.getInt(cursor.getColumnIndex("steps_rank"));
                int streakRank = cursor.getInt(cursor.getColumnIndex("streak_rank"));
                Log.d("DBHelper", String.format(
                    "User: id=%d, name=%s, steps=%d (rank %d), streak=%d (rank %d)", 
                    id, name, steps, stepsRank, streak, streakRank));
            }
            cursor.moveToFirst(); // Reset cursor position
        }
        return cursor;
    }

    // Add method to check if database is empty
    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
        boolean isEmpty = true;
        if (cursor != null && cursor.moveToFirst()) {
            isEmpty = cursor.getInt(0) == 0;
            cursor.close();
        }
        Log.d("DBHelper", "Database is " + (isEmpty ? "empty" : "not empty"));
        return isEmpty;
    }
    
    // Get top 10 users by quest streak with rank
    public Cursor getTop10ByQuestStreak() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT id, name, questStreak, " +
            "RANK() OVER (ORDER BY questStreak DESC) as rank " +
            "FROM users " +
            "ORDER BY questStreak DESC LIMIT 10",
            null
        );
    }

    // Get top 10 users by steps today with rank
    public Cursor getTop10ByStepsToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT id, name, stepsToday, " +
            "RANK() OVER (ORDER BY stepsToday DESC) as rank " +
            "FROM users " +
            "ORDER BY stepsToday DESC LIMIT 10",
            null
        );
    }

    // Update step goal
    public void updateStepGoal(int userId, int goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stepGoal", goal);
        int result = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Updated step goal for user " + userId + " to " + goal + ". Result: " + result);
        
        // Verify the update
        Cursor cursor = db.rawQuery("SELECT stepGoal FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            int updatedGoal = cursor.getInt(0);
            Log.d(TAG, "Verified step goal update - Current value: " + updatedGoal);
            cursor.close();
        }
    }

    // Get step goal
    public int getStepGoal(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT stepGoal FROM users WHERE id = ?", 
            new String[]{String.valueOf(userId)});
        int goal = 0;
        if (cursor != null && cursor.moveToFirst()) {
            goal = cursor.getInt(0);
            Log.d(TAG, "Retrieved step goal " + goal + " for user " + userId);
        }
        if (cursor != null) {
            cursor.close();
        }
        return goal;
    }

    public void updateUserProfile(int userId, String name, String email, String birthday) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Check if name or email already exists for other users
        Cursor cursor = db.rawQuery(
            "SELECT id FROM users WHERE (name = ? OR email = ?) AND id != ?",
            new String[]{name, email, String.valueOf(userId)}
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            // Name or email already exists for another user
            Log.e(TAG, "Name or email already exists for another user");
            cursor.close();
            return;
        }
        if (cursor != null) {
            cursor.close();
        }
        
        values.put("name", name);
        values.put("email", email);
        values.put("birthday", birthday);
        
        int result = db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
        Log.d(TAG, "Updated profile for user " + userId + ". Result: " + result);
    }
} 