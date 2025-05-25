package com.example.questout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class CircularProgressBar extends View {
    private int max = 60;
    private int progress = 60;
    private Paint bgPaint;
    private Paint progressPaint;
    private RectF rectF;

    public CircularProgressBar(Context context) {
        super(context);
        init();
    }
    public CircularProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public CircularProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0x33FFFFFF); // semi-transparent white
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(16f);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(0xFFFF5E9C); // pink
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(16f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }
    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = 24f;
        float size = Math.min(getWidth(), getHeight()) - padding * 2;
        rectF.set(padding, padding, padding + size, padding + size);
        // Draw background circle
        canvas.drawArc(rectF, 0, 360, false, bgPaint);
        // Draw progress arc
        float sweep = 360f * progress / max;
        canvas.drawArc(rectF, -90, sweep, false, progressPaint);
    }
} 