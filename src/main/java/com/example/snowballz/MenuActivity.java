package com.example.snowballz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.ImageView;


public class MenuActivity extends Activity {
    private MediaPlayer backgroundMusic;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        prefs = getSharedPreferences("SnowBallzPrefs", Context.MODE_PRIVATE);
        setContentView(createMenuView());
        backgroundMusic = MediaPlayer.create(this, R.raw.background_music);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f, 0.5f);
        }
    }

    private View createMenuView() {
        GridLayout layout = new GridLayout(this);
        layout.setColumnCount(5); // Povećan broj kolona za bolji raspored
        layout.setBackgroundColor(Color.rgb(135, 206, 235));
        layout.setPadding(50, 50, 50, 50);

        // Naslov na vrhu
        TextView title = new TextView(this);
        title.setText("SnowBallz - Odabir Levela");
        title.setTextSize(40);
        title.setTextColor(Color.WHITE);
        GridLayout.LayoutParams titleParams = new GridLayout.LayoutParams(GridLayout.spec(0, 1, GridLayout.CENTER), GridLayout.spec(0, 5));
        titleParams.setMargins(0, 50, 0, 20);
        layout.addView(title, titleParams);

        // Gumbi za levele na lijevoj strani
        int unlockedLevel = prefs.getInt("unlockedLevel", 1);
        for (int i = 1; i <= 9; i++) {
            final int level = i;
            Button btn = new Button(this);
            btn.setText("Level " + i);

            if (i <= unlockedLevel) {
                btn.setEnabled(true);
                btn.setBackgroundColor(Color.rgb(34, 139, 34));
                btn.setOnClickListener(v -> {
                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    intent.putExtra("level", level);
                    startActivity(intent);
                });
            } else {
                btn.setEnabled(false);
                btn.setText("Level " + i + " 🔒");
                btn.setBackgroundColor(Color.GRAY);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            // Postavljanje gumba u redove 1, 2, 3 i kolone 0, 1, 2
            params.rowSpec = GridLayout.spec(1 + (i - 1) / 3, 1);
            params.columnSpec = GridLayout.spec((i - 1) % 3, 1);
            params.width = 280;
            params.height = 180;
            params.setMargins(20, 10, 20, 10);
            layout.addView(btn, params);
        }

        // Slika na desnoj strani
        ImageView throwerImage = new ImageView(this);
        // Provjeri da li je 'snowballthrower.jpg' ili .png u res/drawable
        throwerImage.setImageResource(R.drawable.snowballthrower);
        throwerImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Postavljanje slike u redove 1-3 i kolone 3-4
        GridLayout.Spec imageRowSpec = GridLayout.spec(1, 3, GridLayout.FILL);
        GridLayout.Spec imageColSpec = GridLayout.spec(3, 2, GridLayout.FILL);
        GridLayout.LayoutParams imageParams = new GridLayout.LayoutParams(imageRowSpec, imageColSpec);
        imageParams.width = 600;
        imageParams.height = 600;
        imageParams.leftMargin = 100; // Dodajemo marginu da odvojimo od gumba
        layout.addView(throwerImage, imageParams);

        return layout;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null) backgroundMusic.start();
        // Osvježi prikaz gumba ako je progres promijenjen
        setContentView(createMenuView());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null) backgroundMusic.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }
}
