package de.ur.mi.audidroid.views

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import com.google.android.material.navigation.NavigationView
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.PermissionHelper
import de.ur.mi.audidroid.utils.ThemeHelper
import kotlinx.android.synthetic.main.app_bar_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val drawerLayout: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val navigationView: NavigationView by lazy {
        findViewById<NavigationView>(
            R.id.nav_view
        )
    }
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.record,
                R.id.files,
                R.id.settings
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        initTheme()
        checkPermissions()
        loadFFMpegLibrary()
    }

    /** Applies the app theme selected by the user.
     *
     * @author: Jonas Puchinger
     * Adapted from: https://www.raywenderlich.com/6488033-android-10-dark-theme-getting-started#toc-anchor-006
     */
    private fun initTheme() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        ThemeHelper.applyTheme(
            preferences.getString(
                getString(R.string.theme_preference_key),
                "default"
            )!!
        )
    }

    private fun checkPermissions() {
        val result = PermissionHelper.permissionsGranted(this@MainActivity)
        if (result != null) PermissionHelper.showDialog(result, this@MainActivity)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED
                ) {
                    checkPermissions()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun loadFFMpegLibrary() {
        try {
            FFmpeg.getInstance(this).loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override fun onFailure() {
                    Log.e("FFMpeg", "Failed to load FFMpeg library.")
                }

                override fun onSuccess() {
                    Log.i("FFMpeg", "FFMpeg Library loaded!")
                }

                override fun onStart() {
                    Log.i("FFMpeg", "FFMpeg Started")
                }

                override fun onFinish() {
                    Log.i("FFMpeg", "FFMpeg Stopped")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
