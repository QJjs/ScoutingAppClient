package us.qjjs.browser

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import us.qjjs.R

class FilePagerAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

	val TITLES = arrayOf(
		context.getString(R.string.tab_finished_forms),
		context.getString(R.string.tab_synced_forms),
		context.getString(R.string.tab_template_forms)
	)

	val LAYOUTS = arrayOf(
		BrowserFragment("forms"),
		BrowserFragment("synced"),
		BrowserFragment("templates")
	)

	override fun getItem(position: Int): Fragment {
		return LAYOUTS[position]
	}

	override fun getPageTitle(position: Int): CharSequence? {
		return TITLES[position]
	}

	override fun getCount(): Int {
		return TITLES.size
	}
}