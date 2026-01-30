package com.example.snowballz;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Slingshot {
    private final float x;
    private final float y;
    private final float width = 20;
    private final float height = 100;

    public Slingshot(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.rgb(101, 67, 33));
        canvas.drawRect(x - width / 2, y, x + width / 2, y + height, paint);

        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(8);
        canvas.drawLine(x - 30, y, x, y - 20, paint);
        canvas.drawLine(x + 30, y, x, y - 20, paint);
    }
}
