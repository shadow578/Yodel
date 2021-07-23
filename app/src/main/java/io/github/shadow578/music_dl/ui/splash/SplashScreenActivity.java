package io.github.shadow578.music_dl.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.shadow578.music_dl.ui.main.MainActivity;
import io.github.shadow578.music_dl.util.Async;

/**
 * basic splash- screen activity.
 * displays a splash- screen, then redirects the user to the correct activity
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // minimum time of 1 seconds
        Async.runLaterOnMain(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 1000);
    }
}
