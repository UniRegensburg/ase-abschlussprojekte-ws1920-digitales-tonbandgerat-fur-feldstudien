package de.ur.mi.audidroid.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.leanback.app.OnboardingSupportFragment
import androidx.preference.PreferenceManager
import de.ur.mi.audidroid.R
import org.jetbrains.anko.doAsync


class OnboardingFragment : OnboardingSupportFragment() {


    private lateinit var titles: Array<String>
    private lateinit var descriptions: Array<String>
    private lateinit var contentView: ImageView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        titles = resources.getStringArray(R.array.onboarding_titles)
        descriptions = resources.getStringArray(R.array.onboarding_descriptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logoResourceId = R.drawable.ic_launcher_round
        titleViewTextColor = ContextCompat.getColor(context!!, R.color.color_on_surface)
        descriptionViewTextColor = ContextCompat.getColor(context!!, R.color.color_on_surface)
    }

    override fun onPageChanged(newPage: Int, previousPage: Int) {
        super.onPageChanged(newPage, previousPage)
        when(newPage){
            1 -> contentView.setImageResource(R.drawable.save)
            2 -> contentView.setImageResource(R.drawable.files)
            3 -> contentView.setImageResource(R.drawable.cut)
            4 -> contentView.setImageResource(R.drawable.settings)
        }
    }

    override fun onCreateContentView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        contentView = layoutInflater.inflate(
            R.layout.onboarding_image, container,
            false
        ) as pl.droidsonroids.gif.GifImageView
        contentView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        return contentView
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
        val background = View(activity)
        background.setBackgroundColor(ContextCompat.getColor(context!!, R.color.color_background))
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
