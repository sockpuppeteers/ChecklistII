package com.example.doug.checklistpresentlayer

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*
import org.jetbrains.anko.textColor


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

    var taskID: Int? = null

    private var taskText = text

    private var taskTextView: TextView

    private var isRecurring = false
    private var isComplete = false

    init
    {
        var tempLayout = RelativeLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)

            //setBackgroundColor(ContextCompat.getColor(context, R.color.colorTaskBackground)
        orientation = LinearLayout.HORIZONTAL

        taskTextView = TextView(context)

        layoutParams = tempLayout

        gravity = right

        tempLayout = RelativeLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        val taskSwitch = CheckBox(context)

        tempLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        //tempLayout.addRule(RelativeLayout.LEFT_OF, taskSwitch.id)

        taskTextView.text = taskText
        taskTextView.textSize = 30f
        taskTextView.layoutParams = tempLayout

        taskTextView.gravity = left



        tempLayout = RelativeLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        tempLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        //tempLayout.addRule(RelativeLayout.RIGHT_OF, taskTextView.id)


        taskSwitch.layoutParams = tempLayout

        //taskSwitch.gravity = right

        addView(taskSwitch, tempLayout)
        addView(taskTextView, tempLayout)
    }

    fun ChangeName(name: String)
    {
        taskText = name

        taskTextView.text = taskText
    }


    fun setRecurringIfNotComplete(reccuring: Boolean) {
        if(!isComplete)
            isRecurring = reccuring

        if(isRecurring)
            taskTextView.textColor = Color.RED
        else
            taskTextView.textColor = Color.BLACK
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
        taskTextView.apply { taskTextView.paintFlags = taskTextView.paintFlags!!.or(Paint.STRIKE_THRU_TEXT_FLAG) }


        getChildAt(0).isClickable = false
        getChildAt(0).visibility = View.INVISIBLE
    }
}