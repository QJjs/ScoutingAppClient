package us.qjjs.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import us.qjjs.R

class FileBrowseActivity : AppCompatActivity() {

	lateinit var pageCollection: FilePagerAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_browser)
		pageCollection = FilePagerAdapter(this, supportFragmentManager)

		val pager: ViewPager = findViewById(R.id.pager)
		pager.adapter = pageCollection
		val tabs: TabLayout = findViewById(R.id.tabs)
		tabs.setupWithViewPager(pager)
	}
}