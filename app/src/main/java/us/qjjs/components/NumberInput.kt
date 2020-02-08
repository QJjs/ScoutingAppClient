package us.qjjs.components

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import us.qjjs.R
import kotlin.math.max
import kotlin.math.min

class NumberInput(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

	private val attributes = context?.obtainStyledAttributes(attrs, R.styleable.NumberInput)

	var minimum = attributes?.getInteger(R.styleable.NumberInput_min, 0) ?: 0
	var maximum = attributes?.getInteger(R.styleable.NumberInput_min, Int.MAX_VALUE) ?: Int.MAX_VALUE
	var label = attributes?.getText(R.styleable.NumberInput_label) ?: ""
		set(value) {
			labelView.text = value
			field = value
			if (value.isBlank()) {
				labelView.visibility = View.GONE
			} else {
				labelView.visibility = View.VISIBLE
			}
		}
	var value: Int?
		set(value) {
			editText.text = Editable.Factory().newEditable(value.toString())
		}
		get() {
			return editText.text.toString().toIntOrNull()
		}


	private val labelView = TextView(context)
	private val minus = Button(context)
	private val editText = EditText(context)
	private val plus = Button(context)

	init {
		orientation = VERTICAL
		val scale = resources.displayMetrics.density
		setPadding((50f * scale).toInt(), 0, (50f * scale).toInt(), 0)

		label = label
		labelView.textAlignment = View.TEXT_ALIGNMENT_CENTER
		addView(labelView)

		val subLayout = LinearLayout(context)
		subLayout.orientation = HORIZONTAL
		addView(subLayout)

		minus.text = context?.getText(R.string.minus)
		plus.text = context?.getText(R.string.plus)
		minus.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)
		plus.layoutParams = minus.layoutParams
		minus.setOnClickListener { decrease() }
		plus.setOnClickListener { increase() }

		editText.layoutParams = LayoutParams((30f * scale).toInt(), LayoutParams.WRAP_CONTENT, 1f)
		editText.inputType = InputType.TYPE_CLASS_NUMBER
		editText.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
		editText.textSize = 24f

		subLayout.addView(minus)
		subLayout.addView(editText)
		subLayout.addView(plus)
	}

	fun increase() {
		val current = editText.text.toString().toIntOrNull()
		editText.text.clear()
		if (current == null) {
			editText.text.append(minimum.toString())
		} else {
			editText.text.append(min(current + 1, minimum).toString())
		}
	}

	fun decrease() {
		val current = editText.text.toString().toIntOrNull()
		editText.text.clear()
		if (current == null) {
			editText.text.append(minimum.toString())
		} else {
			editText.text.append(max(current - 1, minimum).toString())
		}
	}
}