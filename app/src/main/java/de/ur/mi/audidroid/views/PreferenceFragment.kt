package de.ur.mi.audidroid.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.ThemeHelper

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference = findPreference<ListPreference>(getString(R.string.theme_preference_key))!!
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                ThemeHelper.applyTheme(newValue as String)
                true
            }

        val storePreference = findPreference<Preference>(getString(R.string.storage_preference_key))!!
        storePreference.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                    resources.getInteger(R.integer.request_code_set_storage))
                true
            }
    }

    /**
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == resources.getInteger(R.integer.request_code_set_storage)
            && resultCode == Activity.RESULT_OK){
                val preference = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = preference.edit()

                //val cleanedPath = data!!.data!!.path
                var cleanedPath = data!!.data.toString()
                //cleanedPath = cleanedPath.split(":")[1]
                editor.putString(getString(R.string.storage_preference_key), cleanedPath)
                editor.apply()
        }
    }
}
