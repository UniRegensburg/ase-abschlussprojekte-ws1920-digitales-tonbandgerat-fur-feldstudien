package de.ur.mi.audidroid.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.leanback.app.OnboardingSupportFragment
import androidx.preference.PreferenceManager
import de.ur.mi.audidroid.R
import pl.droidsonroids.gif.GifDrawable


class OnboardingFragment : OnboardingSupportFragment() {


    private lateinit var titles: Array<String>
    private lateinit var descriptions: Array<String>
    private lateinit var contentView: pl.droidsonroids.gif.GifImageView
    private lateinit var skipButton: Button


    override fun onAttach(context: Context) {
        super.onAttach(context)
        titles = resources.getStringArray(R.array.onboarding_titles)
        descriptions = resources.getStringArray(R.array.onboarding_descriptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleViewTextColor = ContextCompat.getColor(context!!, R.color.color_surface)
        descriptionViewTextColor = ContextCompat.getColor(context!!, R.color.color_surface)
    }

    override fun onPageChanged(newPage: Int, previousPage: Int) {
        super.onPageChanged(newPage, previousPage)
        when (newPage) {
            1 -> contentView.setImageDrawable(roundEdges(R.drawable.save))
            2 -> contentView.setImageDrawable(roundEdges(R.drawable.files))
            3 -> contentView.setImageDrawable(roundEdges(R.drawable.cut))
            4 -> {
                contentView.setImageDrawable(roundEdges(R.drawable.settings))
                skipButton.visibility = View.GONE
            }
        }
    }

    override fun onCreateContentView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        contentView = layoutInflater.inflate(
            R.layout.onboarding_image, container,
            false
        ) as pl.droidsonroids.gif.GifImageView
        contentView.setImageDrawable(roundEdges(R.drawable.rec))
        contentView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        return contentView
    }

    private fun roundEdges(gifId: Int): GifDrawable {
        val gif = GifDrawable(resources, gifId)
        gif.cornerRadius = 80f
        return gif
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
        skipButton = layoutInflater.inflate(
            R.layout.onboarding_skip, container,
            false
        ) as Button
        skipButton.setOnClickListener {
            onFinishFragment()
        }
        return skipButton
    }

    override fun onCreateBackgroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        val background = View(activity)
        background.setBackgroundColor(ContextCompat.getColor(context!!, R.color.color_primary))
        return background
    }

    override fun onFinishFragment() {
        super.onFinishFragment()
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putBoolean(getString(R.string.onboarding_preference_key), true)
            apply()
        }
        activity!!.finish()
    }
}
