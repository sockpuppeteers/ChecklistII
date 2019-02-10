package com.example.doug.checklistpresentlayer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Switch
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*


/********************************************
 *Class: TaskBox
 *
 *Purpose: Acts as a container for task text
 * in the main checklist view
 ********************************************/
class ListBox @JvmOverloads constructor(
    context: Context,
    text: String,
    attrs: AttributeSet? = null,
    defStyle: Int = 0): LinearLayout(context, attrs, defStyle) {

    val listText = text

    init
    {
            //setBackgroundColor(ContextCompat.getColor(context, R.color.colorTaskBackground))

            orientation = LinearLayout.HORIZONTAL

            val taskTextView = TextView(context)

            layoutParams = LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            gravity = right

            taskTextView.text = listText
        taskTextView.textSize = 30f
        taskTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

//        val taskSwitch = Switch(context)

//        taskSwitch.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT)

        addView(taskTextView)
//        addView(taskSwitch)
    }
}