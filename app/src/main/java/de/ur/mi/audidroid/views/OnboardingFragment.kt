package de.ur.mi.audidroid.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.OnboardingSupportFragment
import androidx.preference.PreferenceManager
import de.ur.mi.audidroid.R


class OnboardingFragment : OnboardingSupportFragment() {

    private val CONTENT_IMAGES = intArrayOf(
        /* R.drawable.gallery_photo_1,
         R.drawable.gallery_photo_2,
         R.drawable.gallery_photo_3*/
    )
    private lateinit var titles: Array<String>
    private lateinit var descriptions: Array<String>


    override fun onAttach(context: Context) {
        super.onAttach(context)
        titles = resources.getStringArray(R.array.onboarding_titles)
        descriptions = resources.getStringArray(R.array.onboarding_descriptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logoResourceId = R.drawable.ic_launcher_round
    }

    override fun onCreateContentView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        return null
    }

    override fun onFinishFragment() {
        super.onFinishFragment()
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putBoolean(getString(R.string.onboarding_preference_key), true)
            apply()
        }
        activity!!.finish()
    }

    override fun getPageCount(): Int {
        return titles.size
    }

    override fun getPageTitle(pageIndex: Int): CharSequence {
        return titles[pageIndex]
    }

    override fun getPageDescription(pageIndex: Int): CharSequence {
        return descriptions[pageIndex]
    }

    override fun onCreateForegroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        return null
    }

    override fun onCreateBackgroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        val bgView = View(activity)
        bgView.setBackgroundColor(resources.getColor(R.color.color_background))
        return bgView
    }
}
