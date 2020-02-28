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
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.ThemeHelper

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initLabelsPreference()
        initThemePreference()
        initStoragePreference()

    }

    private fun initLabelsPreference() {
        val labelsPreference = findPreference<Preference>(getString(R.string.labels_preference_key))!!
        labelsPreference.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                view!!.findNavController().navigate(R.id.action_global_editLabelsFragment)
                true
            }
    }

    private fun initThemePreference() {
        val themePreference = findPreference<ListPreference>(getString(R.string.theme_preference_key))!!
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                ThemeHelper.applyTheme(newValue as String)
                true
            }
    }

    private fun initStoragePreference(){
        val storagePreference = findPreference<Preference>(getString(R.string.storage_preference_key))!!
        storagePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, resources.getInteger(R.integer.activity_request_code_preference_storage))
            true
        }
    }
    private fun setStoragePreferenceSummary(uri: Uri){
        val storagePreference = findPreference<Preference>(getString(R.string.storage_preference_key))
        val summary = uri.pathSegments.last().split(":")[1]
        storagePreference!!.setSummary(summary)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resources.getInteger(R.integer.activity_request_code_preference_storage) &&
                resultCode == Activity.RESULT_OK){
            val pref = context!!.getSharedPreferences(getString(R.string.storage_preference_key), Context.MODE_PRIVATE)
            with (pref.edit()){
                putString(getString(R.string.storage_preference_key), data!!.dataString)
                commit()
            }
            setStoragePreferenceSummary(data!!.data!!)
        }
    }
}
