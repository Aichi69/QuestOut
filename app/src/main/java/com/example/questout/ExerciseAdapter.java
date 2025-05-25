package com.example.questout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private List<QuestTask> exercises;

    public ExerciseAdapter(List<QuestTask> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        QuestTask exercise = exercises.get(position);
        holder.exerciseName.setText(exercise.name);
        holder.exerciseReps.setText(exercise.reps);
        holder.exerciseImage.setImageResource(exercise.imageResId);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView exerciseImage;
        TextView exerciseName;
        TextView exerciseReps;

        ExerciseViewHolder(View itemView) {
            super(itemView);
            exerciseImage = itemView.findViewById(R.id.exerciseImage);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseReps = itemView.findViewById(R.id.exerciseReps);
        }
    }
} 