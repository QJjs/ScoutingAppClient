package us.qjjs

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.json.JSONObject
import java.io.FileReader

class FormPagerAdapter(activity: Activity, fm: FragmentManager) : FragmentPagerAdapter(fm) {

	val titles = ArrayList<String>()
	val layouts = ArrayList<FormFragment>()

	init {
		val file = JSONObject(FileReader((activity as Context).filesDir.resolve("templates/${SettingsFile(activity)["template"]}")).readText())
		for (k in file.keys()) {
			if (k == "title") {
				activity.title = file.getString("title")
				continue
			}
			val components = file.getJSONArray(k)
			val data = ArrayList<ComponentData>()
			for (i in 0 until components.length()) {
				val props = ArrayList<JSONProperty>()
				for (p in components.getJSONObject(i).keys()) {
					props.add(JSONProperty(p, components.getJSONObject(i).get(p)))
				}
				data.add(ComponentData(props))
			}
			layouts.add(FormFragment(data.toTypedArray()))
			titles.add(k)
		}
	}

	override fun getItem(position: Int): Fragment {
		return layouts[position]
	}

	override fun getPageTitle(position: Int): CharSequence? {
		return titles[position]
	}

	override fun getCount(): Int {
		return layouts.size
	}
}
