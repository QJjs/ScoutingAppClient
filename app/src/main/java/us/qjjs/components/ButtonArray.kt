package us.qjjs.components

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import us.qjjs.R

class ButtonArray(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

	private val attributes = context?.obtainStyledAttributes(attrs, R.styleable.ButtonArray)

	var entries: List<String> = listOf()
		set(value) {
			field = value
			buttonView.removeAllViews()
			buttons = Array(entries.size) { ToggleButton(context) }
			for (i in buttons.indices) {
				buttons[i].apply {
					text = entries[i]
					textOn = entries[i]
					textOff = entries[i]
					id = View.generateViewId()
					setOnClickListener {
						if (!multiSelect) {
							for (b in buttons) {
								b.isChecked = b.id == it.id
							}
						}
					}
					layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)
				}
				buttonView.addView(buttons[i])
			}
		}

	var label: String? = null
		set(value) {
			field = value
			if (value == null || value.isEmpty()) {
				labelView.visibility = View.GONE
			} else {
				labelView.text = value
				labelView.visibility = View.VISIBLE
			}
		}

	var multiSelect = attributes?.getBoolean(R.styleable.ButtonArray_multiSelect, false) ?: false

	private var buttons: Array<ToggleButton>
	private val buttonView = LinearLayout(context)
	private val labelView = TextView(context)

	var value: String? = null
		get() {
			if (multiSelect) {
				val s = ArrayList<String>()
				buttons.forEach { if (it.isChecked) s.add(it.text.toString()) }
				return s.joinToString(",")
			} else {
				buttons.forEach { if (it.isChecked) return it.text.toString() }
			}
			return null
		}
		private set

	init {
		orientation = VERTICAL
		label = attributes?.getString(R.styleable.ButtonArray_label)
		labelView.textAlignment = View.TEXT_ALIGNMENT_CENTER
		buttons = Array(entries.size) { ToggleButton(context) }
		entries = attributes?.getString(R.styleable.ButtonArray_entries)?.split(Regex(", *")) ?: listOf()
		buttonView.orientation = HORIZONTAL
		buttonView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		addView(labelView)
		addView(buttonView)
	}
}