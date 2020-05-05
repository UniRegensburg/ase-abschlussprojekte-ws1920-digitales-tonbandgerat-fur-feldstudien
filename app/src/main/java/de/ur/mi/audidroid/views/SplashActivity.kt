package de.ur.mi.audidroid.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.ur.mi.audidroid.R

/**
 * The [SplashActivity] displays the app logo briefly at the start of the app.
 * Afterwards, the [MainActivity] is launched.
 * @author: Jonas Puchinger
 */

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AudidroidTheme_DayNight)

        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
