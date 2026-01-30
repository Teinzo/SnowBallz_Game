package com.example.snowballz;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import java.util.ArrayList;
import java.util.List;

public class SnowBall {
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private final float radius = 25;
    private static final float GRAVITY = 0.5f;
    private final List<PointF> path;

    public SnowBall(float x, float y, float velocityX, float velocityY) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.path = new ArrayList<>();
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

    public List<PointF> getPath() {
        return path;
    }

    public void update() {
        // Dodaj trenutnu poziciju u putanju prije ažuriranja
        path.add(new PointF(x, y));
        
        // Primijeni gravitaciju
        velocityY += GRAVITY;

        // Pomakni loptu
        x += velocityX;
        y += velocityY;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Nacrtaj putanju
        paint.setColor(Color.WHITE);
        paint.setAlpha(100);
        paint.setStrokeWidth(5);
        for (int i = 0; i < path.size() - 1; i++) {
            PointF p1 = path.get(i);
            PointF p2 = path.get(i + 1);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
        }
        paint.setAlpha(255);

        // Nacrtaj grudvu
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.LTGRAY);
        canvas.drawCircle(x - 5, y - 5, radius / 3, paint);
    }
}
