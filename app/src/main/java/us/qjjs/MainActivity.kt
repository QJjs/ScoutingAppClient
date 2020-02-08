package us.qjjs

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import us.qjjs.*
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import us.qjjs.browser.FileBrowseActivity
import java.io.BufferedOutputStream
import java.io.File
import java.net.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

	lateinit var pageCollection: FormPagerAdapter
	lateinit var settings: SettingsFile
	lateinit var pager: ViewPager
	lateinit var tabs: TabLayout

	override fun onCreate(savedInstanceState: Bundle?) {
		checkDefaultFiles()
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)

		pageCollection = FormPagerAdapter(this, supportFragmentManager)
		pager = findViewById(R.id.pager)
		pager.adapter = pageCollection
		pager.offscreenPageLimit = pager.adapter!!.count - 1
		tabs = findViewById(R.id.tabs)
		tabs.setupWithViewPager(pager)

		fab.setOnClickListener {
//			compileData()
			doAsync {
				syncTemplates()
				reloadPager()
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {

		return when (item.itemId) {
			R.id.action_new -> {
				AlertDialog.Builder(this).apply {
					setTitle(R.string.dialog_new)
					setPositiveButton(R.string.yes) { _, _ -> reloadPager() }
					setNegativeButton(R.string.no, null)
				}.show()
				return true
			}
			R.id.action_saved -> {
				startActivity(Intent(this, FileBrowseActivity::class.java))
				return true
			}
			R.id.action_sync -> {
				doAsync { syncTemplates() }
				return true
			}
			R.id.action_server_url -> {
				AlertDialog.Builder(this).apply {
					val layout = LinearLayout(context)
					val input = EditText(context)
					val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
					lp.layoutDirection = LinearLayout.VERTICAL
					val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
					lp.setMargins(margin, 0, margin, 0)
					input.layoutParams = lp
					input.hint = "http://example.com"
					input.text.append(settings["url"].toString())

					layout.addView(input)
					setView(layout)
					setTitle(R.string.action_server_url)
					setPositiveButton(R.string.ok) { _, _ ->
						if (input.text.matches(Regex("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"))) {
							settings["url"] = input.text.toString()
						} else {
							Snackbar.make(this@MainActivity.view, "Invalid Url", Snackbar.LENGTH_SHORT).show()
						}
					}
					setNegativeButton(R.string.cancel, null)
				}.show()
				return true
			}
			R.id.action_template -> {

				val templates = filesDir.resolve("templates").list()
				if (templates == null || templates.isEmpty()) {
					Snackbar.make(this.view, "No templates downloaded, please connect to internet", Snackbar.LENGTH_SHORT).show()
					return true
				}
				var selectedItem = templates.indexOf(settings["template"] as String)
				if (selectedItem == -1) {
					selectedItem = 0
				}

				AlertDialog.Builder(this).apply {
					setSingleChoiceItems(templates, selectedItem) { _, i ->
						selectedItem = i
					}
					setTitle(R.string.action_template)
					setPositiveButton(R.string.ok) { _, _ ->
						settings["template"] = templates[selectedItem]
						reloadPager()
					}
					setNegativeButton(R.string.cancel, null)
				}.show()
				return true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	fun syncTemplates() {
		val listRequest = URL(settings["url"] as String + "/api/templates").openConnection() as HttpURLConnection
		listRequest.requestMethod = "GET"
		listRequest.connectTimeout = 10000

		val response = JSONObject(String(listRequest.inputStream.readBytes())).getJSONArray("files")
		val files = Array(response.length()) { response.getJSONObject(it).getString("file") }
		Snackbar.make(view, files.joinToString(), Snackbar.LENGTH_SHORT).show()
		listRequest.disconnect()
		for (f in files) {
			try {
				val c = URL(settings["url"] as String + "/templates/$f").openConnection() as HttpURLConnection
				Log.i("app", "${settings["url"]}/templates/$f")
				val t = File(filesDir, "templates/$f")
				t.createNewFile()
				t.writeBytes(c.inputStream.readBytes())
				c.disconnect()
			} catch (e: SocketException) {
				Log.e("app", e.toString())
			}
		}
	}

	fun reloadPager() {
		val transaction = supportFragmentManager.beginTransaction()
		supportFragmentManager.fragments.forEach {
			transaction.remove(it)
		}
		transaction.commitNow()
		pageCollection = FormPagerAdapter(this@MainActivity, supportFragmentManager)
		pager.adapter = pageCollection
	}

	fun checkDefaultFiles() {
		arrayOf("forms", "synced", "templates").forEach { dir ->
			val d = File(filesDir, dir)
			if (!d.exists()) {
				d.mkdir()
			}
		}

		settings = SettingsFile(this)
	}

	fun compileData() {
		val dataStream = ArrayList<JSONProperty>()
		for (i in pageCollection.layouts.indices) {
			val fragment = pageCollection.layouts[i]
			val data = fragment.getData()
			if (data == null) {
				Snackbar.make(view, "Please fill in every necessary point of data for " + pageCollection.getPageTitle(i), Snackbar.LENGTH_LONG).show()
				pager.setCurrentItem(i, true)
				return
			}

			dataStream.addAll(data)

			if(fragment.terminateForm()) break
		}
		if (dataStream.isNotEmpty()) {
			val file = File(filesDir, "forms/${Calendar.getInstance().time.toString().replace(":", "-")}.json")
			file.createNewFile()
			val obj = JSONObject()
			obj.put("template", title)
			for(s in dataStream) {
				obj.put(s.name, s.value)
			}
			file.writeText(obj.toString())
		}
	}

	fun sendFile(fileName: String) {

		val connection = URL("http://bbq.qjjs.us").openConnection() as HttpURLConnection
		connection.doOutput = true
		connection.connectTimeout = 10000
		connection.requestMethod = "POST"
		connection.setRequestProperty("Date", fileName)

		connectionQuery()

		try {
			connection.connect()
			val out = BufferedOutputStream(connection.outputStream)
			out.write(File(fileName).readBytes())
			Snackbar.make(view, "Connected!", Snackbar.LENGTH_LONG).show()
		} catch(e: Exception) {
			Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show()
		}

		Snackbar.make(view, "Finished file $fileName with code ${connection.responseCode}", Snackbar.LENGTH_SHORT).show()
		connection.disconnect()
	}

	fun connectionQuery() {
		doAsync {
			val sb = Snackbar.make(view, "Connecting", Snackbar.LENGTH_INDEFINITE)
			sb.show()
			val sbText = sb.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
			var stage = 0
			var stages = arrayOf("Connecting.", "Connecting..", "Connecting...")
			Thread.sleep(700)
			while (sb.isShown) {
				runOnUiThread { sbText.text = stages[stage++] }
				Thread.sleep(700)
				if (stage >= stages.size) {
					stage = 0
				}
			}
		}
	}
}
