package de.ur.mi.audidroid.views

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.app_bar_main.*
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.ThemeHelper


class MainActivity : AppCompatActivity() {

    private val drawerLayout: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val navigationView: NavigationView by lazy { findViewById<NavigationView>(
        R.id.nav_view
    ) }
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
    }

    /** Applies the app theme selected by the user.
     *
     * @author: Jonas Puchinger
     * Adapted from: https://www.raywenderlich.com/6488033-android-10-dark-theme-getting-started#toc-anchor-006
     */
    private fun initTheme() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        ThemeHelper.applyTheme(preferences.getString(getString(R.string.theme_preference_key), "default")!!)
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
}

