package us.qjjs

import android.content.Context
import org.json.JSONObject
import java.io.File

class SettingsFile(context: Context) {
	private val file = File(context.filesDir, "settings.json")
	private val data: JSONObject

	init {
		if (!file.exists()) {
			file.createNewFile()
			data = JSONObject()
			set("url", "http://bbq.qjjs.us")
			set("template", "2020_main.json")
		} else {
			data = JSONObject(file.readText())
		}
	}

	operator fun set(name: String, value: Any) {
		data.put(name, value)
		file.writeText(data.toString())
	}

	operator fun get(name: String): Any {
		return data.get(name)
	}
}