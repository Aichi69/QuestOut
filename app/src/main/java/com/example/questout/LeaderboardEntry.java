package com.example.questout;

public class LeaderboardEntry {
    private int rank;
    private String username;
    private int value;

    public LeaderboardEntry(int rank, String username, int value) {
        this.rank = rank;
        this.username = username;
        this.value = value;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getValue() {
        return value;
    }
} 