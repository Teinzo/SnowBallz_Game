package com.example.snowballz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private final SurfaceHolder holder;
    private volatile boolean playing;
    private final Paint paint;

    private Slingshot slingshot;
    private final List<SnowBall> snowballs;
    private final List<Target> targets;
    private final List<Snowflake> snowflakes;
    private List<PointF> lastSnowballPath;
    private PointF touchCurrent;
    private boolean isDragging = false;

    private int screenWidth;
    private int screenHeight;

    private int snowballsLeft = 5;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int currentLevel;
    
    private Rect resetButton;
    private Rect backToMenuButton;

    private SoundPool soundPool;
    private int soundStretch, soundThrow, soundHit;
    private MediaPlayer backgroundMusic;
    private int stretchStreamId = 0;




    public GameView(Context context, int level) {
        super(context);
        this.currentLevel = level;
        holder = getHolder();
        paint = new Paint();

        snowballs = new ArrayList<>();
        targets = new ArrayList<>();
        snowflakes = new ArrayList<>();
        lastSnowballPath = new ArrayList<>();
        resetButton = new Rect();
        backToMenuButton = new Rect();

        initSounds(context);
    }

    private void initSounds(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundStretch = soundPool.load(context, R.raw.slingshot_stretch, 1);
        soundThrow = soundPool.load(context, R.raw.snowball_throw, 1);
        soundHit = soundPool.load(context, R.raw.target_hit, 1);

        backgroundMusic = MediaPlayer.create(context, R.raw.background_music);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.3f, 0.3f);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        slingshot = new Slingshot(250, screenHeight - 300f);
        createTargetsForLevel(currentLevel);
        createSnow();
    }

    private void createTargetsForLevel(int level) {
        targets.clear();
        switch(level) {
            case 1:
                targets.add(new Target(screenWidth - 400, screenHeight - 200, 60));
                targets.add(new Target(screenWidth - 250, screenHeight - 200, 60));
                targets.add(new Target(screenWidth - 550, screenHeight - 350, 60));
                break;
            case 2:
                targets.add(new Target(screenWidth - 300, screenHeight - 200, 50));
                targets.add(new Target(screenWidth - 450, screenHeight - 300, 50));
                targets.add(new Target(screenWidth - 600, screenHeight - 400, 50));
                targets.add(new Target(screenWidth - 150, screenHeight - 500, 50));
                break;
            default:
                for(int i = 0; i < 3 + level; i++) {
                    float tx = screenWidth / 2f + (float)Math.random() * (screenWidth / 2f - 100);
                    float ty = screenHeight / 2f + (float)Math.random() * (screenHeight / 2f - 300);
                    targets.add(new Target(tx, ty, Math.max(20, 50 - level)));
                }
                break;
        }
        snowballsLeft = targets.size() + 2;
    }

    private void createSnow() {
        snowflakes.clear();
        for (int i = 0; i < 100; i++) {
            snowflakes.add(new Snowflake(screenWidth, screenHeight));
        }
    }

    private void saveProgress() {
        SharedPreferences prefs = getContext().getSharedPreferences("SnowBallzPrefs", Context.MODE_PRIVATE);
        int currentUnlocked = prefs.getInt("unlockedLevel", 1);
        if (currentLevel + 1 > currentUnlocked) {
            prefs.edit().putInt("unlockedLevel", currentLevel + 1).apply();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tx = event.getX();
        float ty = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (gameOver || gameWon) {
                    if (backToMenuButton.contains((int) tx, (int) ty)) {
                        ((Activity) getContext()).finish();
                    } else if (gameOver && resetButton.contains((int) tx, (int) ty)) {
                        resetGame();
                    }
                } else if (snowballsLeft > 0) {
                    PointF touchStart = new PointF(tx, ty);
                    if (isNearSlingshot(touchStart)) {
                        isDragging = true;
                        touchCurrent = new PointF(touchStart.x, touchStart.y);
                        stretchStreamId = soundPool.play(soundStretch, 1, 1, 1, -1, 1.0f);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) touchCurrent = new PointF(tx, ty);
                break;

            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    if (stretchStreamId != 0) {
                        soundPool.stop(stretchStreamId);
                        stretchStreamId = 0;
                    }
                    soundPool.play(soundThrow, 1, 1, 1, 0, 1.0f);
                    launchSnowball();
                    isDragging = false;
                    touchCurrent = null;
                }
                break;
        }
        return true;
    }

    private void resetGame() {
        gameOver = false;
        gameWon = false;
        snowballs.clear();
        lastSnowballPath.clear();
        createTargetsForLevel(currentLevel);
    }

    private boolean isNearSlingshot(PointF point) {
        if (slingshot == null) return false;
        float dx = point.x - slingshot.getX();
        float dy = point.y - slingshot.getY();
        return Math.sqrt(dx * dx + dy * dy) < 100;
    }

    private void launchSnowball() {
        if (slingshot == null || touchCurrent == null || snowballsLeft <= 0 || gameWon) return;
        float dx = slingshot.getX() - touchCurrent.x;
        float dy = slingshot.getY() - touchCurrent.y;
        float velocityX = dx * 0.15f;
        float velocityY = dy * 0.15f;
        SnowBall snowball = new SnowBall(slingshot.getX(), slingshot.getY(), velocityX, velocityY);
        snowballs.add(snowball);
        snowballsLeft--;
    }

    private void update() {
        for (Snowflake snowflake : snowflakes) snowflake.update();
        if (gameOver || gameWon) return;

        Iterator<SnowBall> snowballIterator = snowballs.iterator();
        while (snowballIterator.hasNext()) {
            SnowBall snowball = snowballIterator.next();
            snowball.update();

            if (snowball.getY() > screenHeight || snowball.getX() < 0 || snowball.getX() > screenWidth) {
                lastSnowballPath = new ArrayList<>(snowball.getPath());
                snowballIterator.remove();
                continue;
            }

            Iterator<Target> targetIterator = targets.iterator();
            while (targetIterator.hasNext()) {
                Target target = targetIterator.next();
                if (checkCollision(snowball, target)) {
                    soundPool.play(soundHit, 1, 1, 1, 0, 1.0f);
                    lastSnowballPath = new ArrayList<>(snowball.getPath());
                    targetIterator.remove();
                    snowballIterator.remove();
                    if (targets.isEmpty()) {
                        gameWon = true;
                        saveProgress();
                    }
                    break;
                }
            }
        }

        if (snowballsLeft == 0 && snowballs.isEmpty() && !targets.isEmpty()) {
            gameOver = true;
        }
    }

    private boolean checkCollision(SnowBall snowball, Target target) {
        float dx = snowball.getX() - target.getX();
        float dy = snowball.getY() - target.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < (snowball.getRadius() + target.getRadius());
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if (canvas == null) return;
            canvas.drawColor(Color.rgb(135, 206, 235));
            for (Snowflake snowflake : snowflakes) snowflake.draw(canvas, paint);
            paint.setColor(Color.WHITE);
            canvas.drawRect(0, screenHeight - 250, screenWidth, screenHeight, paint);
            paint.setColor(Color.rgb(240, 240, 240));
            for (int i = 0; i < screenWidth; i += 50) canvas.drawCircle(i, screenHeight - 250, 10, paint);
            paint.setColor(Color.rgb(139, 69, 19));
            canvas.drawRect(screenWidth - 700, screenHeight - 600, screenWidth - 100, screenHeight - 250, paint);
            paint.setColor(Color.WHITE);
            canvas.drawRect(screenWidth - 710, screenHeight - 610, screenWidth - 90, screenHeight - 590, paint);
            paint.setColor(Color.rgb(70, 130, 180));
            canvas.drawRect(screenWidth - 650, screenHeight - 550, screenWidth - 550, screenHeight - 400, paint);
            canvas.drawRect(screenWidth - 500, screenHeight - 550, screenWidth - 400, screenHeight - 400, paint);

            if (!lastSnowballPath.isEmpty()) {
                paint.setColor(Color.WHITE); paint.setAlpha(80); paint.setStrokeWidth(3);
                for (int i = 0; i < lastSnowballPath.size() - 1; i++) {
                    PointF p1 = lastSnowballPath.get(i); PointF p2 = lastSnowballPath.get(i + 1);
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                }
                paint.setAlpha(255);
            }

            if (slingshot != null) {
                slingshot.draw(canvas, paint);
                if (isDragging && touchCurrent != null) {
                    paint.setColor(Color.BLACK); paint.setStrokeWidth(5);
                    canvas.drawLine(slingshot.getX(), slingshot.getY(), touchCurrent.x, touchCurrent.y, paint);
                }
            }

            for (SnowBall snowball : snowballs) snowball.draw(canvas, paint);
            for (Target target : targets) target.draw(canvas, paint);

            paint.setColor(Color.BLACK); paint.setTextSize(50);
            canvas.drawText("Grude: " + snowballsLeft, 50, 100, paint);
            canvas.drawText("Level: " + currentLevel, 50, 170, paint);

            if (gameOver) {
                drawEndScreen(canvas, "Ti si neuspjeh", "Znam", Color.RED, resetButton, "Meni", backToMenuButton);
            } else if (gameWon) {
                drawEndScreen(canvas, "Wow čestitam", "Meni", Color.GREEN, backToMenuButton, "", null);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawEndScreen(Canvas canvas, String message, String btn1Text, int btnColor, Rect btn1Rect, String btn2Text, Rect btn2Rect) {
        paint.setColor(Color.argb(180, 0, 0, 0));
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        paint.setColor(Color.WHITE); paint.setTextSize(100);
        float textWidth = paint.measureText(message);
        canvas.drawText(message, (screenWidth - textWidth) / 2, screenHeight / 2 - 100, paint);

        // Gumb 1
        paint.setColor(btnColor);
        int btnWidth = 300; int btnHeight = 100;
        btn1Rect.set(screenWidth / 2 - btnWidth - 20, screenHeight / 2, screenWidth / 2 - 20, screenHeight / 2 + btnHeight);
        if (btn2Rect == null) btn1Rect.set(screenWidth / 2 - btnWidth / 2, screenHeight / 2, screenWidth / 2 + btnWidth / 2, screenHeight / 2 + btnHeight);
        canvas.drawRect(btn1Rect, paint);
        paint.setColor(Color.WHITE); paint.setTextSize(50);
        float b1tWidth = paint.measureText(btn1Text);
        canvas.drawText(btn1Text, btn1Rect.centerX() - b1tWidth / 2, btn1Rect.centerY() + 20, paint);

        // Gumb 2 (ako postoji)
        if (btn2Rect != null) {
            paint.setColor(Color.BLUE);
            btn2Rect.set(screenWidth / 2 + 20, screenHeight / 2, screenWidth / 2 + btnWidth + 20, screenHeight / 2 + btnHeight);
            canvas.drawRect(btn2Rect, paint);
            paint.setColor(Color.WHITE);
            float b2tWidth = paint.measureText(btn2Text);
            canvas.drawText(btn2Text, btn2Rect.centerX() - b2tWidth / 2, btn2Rect.centerY() + 20, paint);
        }
    }

    @Override
    public void run() {
        while (playing) { update(); draw(); try { Thread.sleep(16); } catch (InterruptedException e) { } }
    }

    public void pause() {
        playing = false;
        if (backgroundMusic != null) backgroundMusic.pause();
        try { if (gameThread != null) gameThread.join(); } catch (InterruptedException e) { }
    }

    public void resume() {
        playing = true;
        if (backgroundMusic != null) backgroundMusic.start();
        gameThread = new Thread(this);
        gameThread.start();
    }
}
