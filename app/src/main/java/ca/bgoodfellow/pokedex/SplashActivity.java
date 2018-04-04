package ca.bgoodfellow.pokedex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class SplashActivity extends AppCompatActivity {
    AnimationDrawable trainerAnimation;

    // Duration of the splash screen
    private final int SPLASH_DISPLAY_LENGTH = 2500;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);

        ImageView ivTrainerPikachu = (ImageView) findViewById(R.id.ivTrainerPikachu);
        ivTrainerPikachu.setBackgroundResource(R.drawable.trainer_walking);
        trainerAnimation = (AnimationDrawable) ivTrainerPikachu.getBackground();


        trainerAnimation.start();

        /* New Handler to start the MainActivity
         * and close this Splash-Screen after a specified amount of seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
