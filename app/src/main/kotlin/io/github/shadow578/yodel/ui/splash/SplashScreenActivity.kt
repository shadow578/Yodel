package io.github.shadow578.yodel.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.shadow578.yodel.ui.main.MainActivity
import io.github.shadow578.yodel.util.launchMain
import kotlinx.coroutines.delay

/**
 * basic splash- screen activity.
 * displays a splash- screen, then redirects the user to the correct activity
 */
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchMain {
            delay(50)
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }
    }
}