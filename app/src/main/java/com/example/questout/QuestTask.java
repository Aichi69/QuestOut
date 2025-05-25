package com.example.questout;

import android.os.Parcel;
import android.os.Parcelable;

public class QuestTask implements Parcelable {
    public String name;
    public String reps;
    public int imageResId;
    public boolean isCompleted;
    public boolean isHeader;
    public boolean isTimerTask;

    public QuestTask(String name, String reps, int imageResId, boolean isTimerTask) {
        this.name = name;
        this.reps = reps;
        this.imageResId = imageResId;
        this.isCompleted = false;
        this.isHeader = false;
        this.isTimerTask = isTimerTask;
    }

    public QuestTask(String name, boolean isHeader) {
        this.name = name;
        this.reps = "";
        this.imageResId = 0;
        this.isCompleted = false;
        this.isHeader = isHeader;
        this.isTimerTask = false;
    }

    protected QuestTask(Parcel in) {
        name = in.readString();
        reps = in.readString();
        imageResId = in.readInt();
        isCompleted = in.readByte() != 0;
        isHeader = in.readByte() != 0;
        isTimerTask = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(reps);
        dest.writeInt(imageResId);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
        dest.writeByte((byte) (isHeader ? 1 : 0));
        dest.writeByte((byte) (isTimerTask ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QuestTask> CREATOR = new Creator<QuestTask>() {
        @Override
        public QuestTask createFromParcel(Parcel in) {
            return new QuestTask(in);
        }

        @Override
        public QuestTask[] newArray(int size) {
            return new QuestTask[size];
        }
    };

    public boolean isHeader() {
        return isHeader;
    }
} 