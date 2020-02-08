package us.qjjs.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.ToggleButton
import us.qjjs.R

class ButtonArray(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

	private val attributes = context?.obtainStyledAttributes(attrs, R.styleable.ButtonArray)

	var labels = attributes?.getString(R.styleable.ButtonArray_labels)?.split(Regex(", *")) ?: listOf()
		set(value) {
			field = value
			refreshButtons()
		}

	var buttons = Array(labels.size) { ToggleButton(context) }

	var value: String? = null
		get() {
			buttons.forEach { if (it.isChecked) return it.text.toString() }
			return null
		}
		private set

	init {
		orientation = HORIZONTAL
		refreshButtons()
	}

	fun refreshButtons() {
		removeAllViews()
		buttons = Array(labels.size) { ToggleButton(context) }
		for (i in buttons.indices) {
			buttons[i].apply {
				text = labels[i]
				textOn = labels[i]
				textOff = labels[i]
				id = View.generateViewId()
				setOnClickListener {
					for (b in buttons) {
						b.isChecked = b.id == it.id
					}
				}
				layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)
			}
			addView(buttons[i])
		}
	}
}