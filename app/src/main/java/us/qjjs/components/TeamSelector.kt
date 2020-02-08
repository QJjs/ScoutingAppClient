package us.qjjs.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.ToggleButton
import us.qjjs.R

class TeamSelector(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

	private val attributes = context?.obtainStyledAttributes(attrs, R.styleable.TeamSelector)

	var count = attributes?.getInt(R.styleable.TeamSelector_count, 3) ?: 3
		set(value) {
			field = value
		}

	var value: String? = null
		get() {
			for (b in buttons) {
				if (b.isChecked) return b.text.toString()
			}
			return null
		}
		private set

	val blueRow = LinearLayout(context)
	val redRow = LinearLayout(context)
	val buttons = Array(count * 2) { ToggleButton(context) }

	init {
		val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		blueRow.apply {
			layoutParams = lp
			orientation = HORIZONTAL
		}

		redRow.apply {
			layoutParams = lp
			orientation = HORIZONTAL
		}

		for (i in 0 until count) {
			buttons[i].apply {
				text = "Red ${i + 1}"
				textOn = "Red ${i + 1}"
				textOff = "Red ${i + 1}"
				id = View.generateViewId()
				setBackgroundResource(R.drawable.team_button_red)
				setOnClickListener {
					for (b in buttons) {
						b.isChecked = b.id == it.id
					}
				}
				layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)
			}

			buttons[i + count].apply {
				text = "Blue ${i + 1}"
				textOn = "Blue ${i + 1}"
				textOff = "Blue ${i + 1}"
				id = View.generateViewId()
				setBackgroundResource(R.drawable.team_button_blue)
				setOnClickListener {
					for (b in buttons) {
						b.isChecked = b.id == it.id
					}
				}
				layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)
			}

			redRow.addView(buttons[i])
			blueRow.addView(buttons[i + count])
		}

		orientation = VERTICAL
		addView(redRow)
		addView(blueRow)
	}
}