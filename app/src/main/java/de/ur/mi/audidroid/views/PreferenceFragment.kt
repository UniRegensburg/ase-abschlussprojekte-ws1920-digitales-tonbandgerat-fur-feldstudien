package de.ur.mi.audidroid.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.Pathfinder
import de.ur.mi.audidroid.utils.ThemeHelper
import kotlinx.android.synthetic.main.content_main.*

class PreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var storagePreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initLabelsPreference()
        initThemePreference()
        initStoragePreference()
    }

    private fun initLabelsPreference() {
        val labelsPreference =
            findPreference<Preference>(getString(R.string.labels_preference_key))!!
        labelsPreference.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                view!!.findNavController().navigate(R.id.action_global_editLabelsFragment)
                true
            }
    }

    private fun initThemePreference() {
        val themePreference =
            findPreference<ListPreference>(getString(R.string.theme_preference_key))!!
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                ThemeHelper.applyTheme(newValue as String)
                true
            }
    }

   private fun initStoragePreference() {
        storagePreference =
            findPreference<Preference>(getString(R.string.storage_preference_key))!!
        storagePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
           Pathfinder.openPathDialog(storagePreference, context!!)
            true
        }
    }

    fun setStoragePreferenceSummary(preference: Preference, context: Context, data: Intent?) {
        val pref = context.getSharedPreferences(
            context.resources.getString(R.string.storage_preference_key),
            Context.MODE_PRIVATE
        )
        with(pref.edit()) {
            putString(context.resources.getString(R.string.storage_preference_key), data!!.dataString)
            commit()
        }
        val summary = data!!.data!!.pathSegments.last().split(":")[1]
        preference.summary = summary
    }
}
