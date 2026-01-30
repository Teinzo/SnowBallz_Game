package com.example.snowballz;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Target {
    private final float x;
    private final float y;
    private final float radius;

    public Target(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Tijelo
        paint.setColor(Color.rgb(255, 192, 203));
        canvas.drawCircle(x, y - radius / 2, radius / 2, paint);

        // Glava
        paint.setColor(Color.rgb(255, 220, 177));
        canvas.drawCircle(x, y - radius, radius / 3, paint);

        // Oči
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x - 8, y - radius - 5, 3, paint);
        canvas.drawCircle(x + 8, y - radius - 5, 3, paint);
    }
}
