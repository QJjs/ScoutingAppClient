package us.qjjs.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import us.qjjs.R
import java.io.File
import kotlin.collections.ArrayList

class BrowserFragment(val folder: String) : Fragment() {

	lateinit var recyclerView: RecyclerView
	lateinit var viewAdapter: FileViewAdapter
	lateinit var viewManager: RecyclerView.LayoutManager
	lateinit var files: ArrayList<File>

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_browser, container, false)

		val directory = activity!!.filesDir.path + "/" + folder
		files = File(directory).listFiles()?.toCollection(ArrayList()) ?: arrayListOf()

		if (files.isEmpty()) {
			view.findViewById<TextView>(R.id.no_files).visibility = View.VISIBLE
		}

		viewAdapter = FileViewAdapter(context!!, files)
		viewManager = LinearLayoutManager(activity)

		recyclerView = view.findViewById(R.id.recycle_view)
		recyclerView.apply {
			setHasFixedSize(true)
			layoutManager = viewManager
			adapter = viewAdapter
		}
		return view
	}
}
