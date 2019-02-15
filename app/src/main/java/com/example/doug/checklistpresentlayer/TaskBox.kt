package com.example.doug.checklistpresentlayer

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
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
class TaskBox @JvmOverloads constructor(
    context: Context,
    text: String,
    attrs: AttributeSet? = null,
    defStyle: Int = 0): LinearLayout(context, attrs, defStyle) {


    private val taskText = text

    private var taskTextView: TextView? = null

    private var isRecurring = false
    private var isComplete = false

    init
    {
            //setBackgroundColor(ContextCompat.getColor(context, R.color.colorTaskBackground))

            orientation = LinearLayout.HORIZONTAL

            taskTextView = TextView(context)

            layoutParams = LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            gravity = right

        taskTextView?.text = taskText
        taskTextView?.textSize = 30f
        taskTextView?.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        val taskSwitch = Switch(context)

        taskSwitch.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        addView(taskTextView)
        addView(taskSwitch)
    }

    fun toggleReccurringIfNotComplete() {
        if(!isComplete) {
            isRecurring = !isRecurring

            if (isRecurring)
                taskTextView?.setTextColor(Color.RED)
            else
                taskTextView?.setTextColor(Color.BLACK)
        }
    }

    fun checkCompletion():  Boolean {
        return isComplete
    }
    
    fun checkReccurring(): Boolean {
        return isRecurring
    }
    
    fun getTaskText(): String {
        return taskText
    }

    fun completeTask(){
        isComplete = true
        if (taskTextView != null) {
            taskTextView.apply { taskTextView?.paintFlags = taskTextView?.paintFlags!!.or(Paint.STRIKE_THRU_TEXT_FLAG) }
        }

        getChildAt(1).isClickable = false
        getChildAt(1).visibility = View.INVISIBLE
    }
}