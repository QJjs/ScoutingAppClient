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
		val componentType = properties["type"]?.toString() ?: "unknown"

		var newView: View? = null
		var lp: LinearLayout.LayoutParams? = null
		when (componentType) {
			"plain_text" -> {
				newView = TextView(context)
				newView.text = properties["message"] as String
				if (properties["format"] is String) {
					val format = properties["format"] as String
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
				newView.text = properties["label"] as String
				lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
				lp.gravity = Gravity.CENTER_HORIZONTAL
			}
			"edit_text" -> {
				newView = EditText(context)
				if (properties["hint"] is String) {
					newView.hint = properties["hint"] as String
				}
				if (properties["multiline"] is Boolean) {
					newView.isSingleLine = !(properties["multiline"] as Boolean)
				}
			}
			"number" -> {
				newView = NumberInput(context)
				if (properties["min"] is Int) {
					newView.minimum = properties["min"] as Int
				}

				if (properties["max"] is Int) {
					newView.maximum = properties["max"] as Int
				}

				if (properties["label"] is String) {
					newView.label = properties["label"] as String
				}
			}
			"list" -> {
				newView = Spinner(context)
				val entries = properties["entries"] as JSONArray
				val items = Array(entries.length() + 1) {
					if (it == 0) {
						if (properties["label"] is String) properties["label"] as String
						else ""
					}
					else entries.getString(it - 1)
				}
				newView.adapter = ArrayAdapter<CharSequence>(context!!, android.R.layout.simple_list_item_1, items)
			}
			"button_array" -> {
				newView = ButtonArray(context)
				val entries = properties["entries"] as JSONArray
				newView.label = properties["label"] as String?
				newView.entries = List<String>(entries.length()) { entries.getString(it) }
				if (properties["multi_select"] is Boolean) {
					newView.multiSelect = properties["multi_select"] as Boolean
				}
			}
			"team" -> {
				newView = TeamSelector(context)
				if (properties["count"] is Int) {
					newView.count = properties["count"] as Int
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
			idLinks.add(IDPair(properties["id"] as String, newView.id, (properties["terminate"] ?: false) as Boolean))
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
					data.add(JSONProperty(it.name, v.text.toString()))
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