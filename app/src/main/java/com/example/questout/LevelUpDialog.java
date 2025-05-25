package com.example.questout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class LevelUpDialog extends Dialog {
    private int newLevel;
    private OnContinueListener listener;

    public interface OnContinueListener {
        void onContinue();
    }

    public LevelUpDialog(Context context, int newLevel, OnContinueListener listener) {
        super(context);
        this.newLevel = newLevel;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_level_up);
        
        // Make dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Set new level text
        TextView newLevelText = findViewById(R.id.newLevelText);
        newLevelText.setText("You are now level " + newLevel);

        // Set continue button click listener
        Button continueBtn = findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinue();
            }
            dismiss();
        });

        // Prevent dialog from being dismissed by clicking outside
        setCancelable(false);
    }
} 