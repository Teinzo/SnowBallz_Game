package com.example.snowballz;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.Random;

public class Snowflake {
    private float x, y;
    private float speed;
    private float radius;
    private final int screenWidth, screenHeight;
    private final Random random;

    public Snowflake(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random();
        reset();
        // Nasumično postavi početnu visinu tako da ne počnu svi s vrha odjednom
        this.y = random.nextInt(screenHeight);
    }

    private void reset() {
        this.x = random.nextInt(screenWidth);
        this.y = -10;
        this.speed = 2 + random.nextFloat() * 5;
        this.radius = 2 + random.nextFloat() * 5;
    }

    public void update() {
        y += speed;
        x += Math.sin(y / 20.0) * 2; // Lagano vrludanje lijevo-desno

        if (y > screenHeight) {
            reset();
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
    }
}
