package us.qjjs

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import org.json.JSONArray
import us.qjjs.components.ButtonArray
import us.qjjs.components.NumberInput
import us.qjjs.components.TeamSelector
import java.lang.Exception

class FormFragment(val elementData: Array<ComponentData>) : Fragment() {

	val idLinks = ArrayList<IDPair>(elementData.size)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val v = inflater.inflate(R.layout.fragment_form, container, false) as ScrollView
		elementData.forEach { createElement(it, v[0]) }
		return v
	}

	fun createElement(properties: ComponentData, view: View? = this.view) {
		val componentType = properties["type"]?.value ?: "unknown"

		var newView: View? = null
		var lp: LinearLayout.LayoutParams? = null
		when (componentType) {
			"plain_text" -> {
				newView = TextView(context)
				newView.text = properties["message"]?.value as String
				if (properties["format"]?.value is String) {
					val format = properties["format"]?.value as String
					if (format.isNotEmpty()) {
						newView.setTextSize(TypedValue.COMPLEX_UNIT_SP,
							when (format) {
								"head" -> 22f
								"plain" -> 14f
								else -> 14f
							}
						)
					}
				}
			}
			"check" -> {
				newView = CheckBox(context)
				newView.text = properties["label"]?.value as String
				lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
				lp.gravity = Gravity.CENTER_HORIZONTAL
			}
			"edit_text" -> {
				newView = EditText(context)
				if (properties["hint"]?.value is String) {
					newView.hint = properties["hint"]?.value as String
				}
				if (properties["multiline"]?.value is Boolean) {
					newView.isSingleLine = !(properties["multiline"]?.value as Boolean)
				}
			}
			"number" -> {
				newView = NumberInput(context)
				if (properties["min"]?.value is Int) {
					newView.minimum = properties["min"]?.value as Int
				}

				if (properties["max"]?.value is Int) {
					newView.maximum = properties["max"]?.value as Int
				}

				if (properties["label"]?.value is String) {
					newView.label = properties["label"]?.value as String
				}
			}
			"list" -> {
				newView = Spinner(context)
				val entries = properties["entries"]?.value as JSONArray
				val items = Array(entries.length() + 1) {
					if (it == 0) {
						if (properties["label"]?.value is String) properties["label"]?.value as String
						else ""
					}
					else entries.getString(it - 1)
				}
				newView.adapter = ArrayAdapter<CharSequence>(context!!, android.R.layout.simple_list_item_1, items)
			}
			"button_array" -> {
				newView = ButtonArray(context)
				val labels = properties["entries"]?.value as JSONArray
				newView.labels = List<String>(labels.length()) { labels.getString(it) }
			}
			"team" -> {
				newView = TeamSelector(context)
				if (properties["count"]?.value is Int) {
					newView.count = properties["count"]?.value as Int
				}
			}
		}

		if (newView == null) {
			return
		}

		if (lp == null) {
			lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
		}
		lp.bottomMargin = resources.getDimension(R.dimen.element_spacing).toInt()
		newView.layoutParams = lp
		newView.id = View.generateViewId()
		if (properties["id"] != null) {
			idLinks.add(IDPair(properties["id"]?.value as String, newView.id, (properties["terminate"]?.value ?: false) as Boolean))
		}
		(view as LinearLayout).addView(newView)
	}

	fun getData(): Array<JSONProperty>? {
		val data = ArrayList<JSONProperty>(elementData.size)
		idLinks.forEach {
			val v = view?.findViewById<View>(it.id) ?: throw Exception("view not found for ${it.name}")
			when (v) {
				is CheckBox -> {
					data.add(JSONProperty(it.name, v.isChecked))
					if (it.terminate && v.isChecked) {
						return data.toTypedArray()
					}
				}
				is EditText -> {
					val text = v.text.toString()
					if (text.isBlank()) {
						return null
					}
				}
				is NumberInput -> {
					data.add(JSONProperty(it.name, v.value ?: return null))
				}
				is Spinner -> {
					if (v.selectedItemPosition == 0) return null
					data.add(JSONProperty(it.name, v.adapter.getItem(v.selectedItemPosition) as String))
				}
				is ButtonArray -> {
					data.add(JSONProperty(it.name, v.value ?: return null))
				}
				is TeamSelector -> {
					data.add(JSONProperty(it.name, v.value ?: return null))
				}
			}
		}
		return data.toTypedArray()
	}

	fun terminateForm(): Boolean {
		idLinks.forEach {
			if (it.terminate) {
				val v = view?.findViewById<View>(it.id) ?: throw Exception("view not found for ${it.name}")
				when (v) {
					is CheckBox -> {
						if(v.isChecked) return true
					}
				}
			}
		}
		return false
	}
}