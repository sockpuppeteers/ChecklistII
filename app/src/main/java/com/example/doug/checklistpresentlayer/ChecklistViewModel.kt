package com.example.doug.checklistpresentlayer

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.textColor

class ChecklistViewModel(var ChecklistText: String) {
    var taskID: Int = 0

    var isRecurring = false
    var isComplete = false
    var isChecked = false


    fun setRecurringIfNotComplete(reccuring: Boolean) {
        if(!isComplete)
            isRecurring = reccuring
    }

    fun completeTask(){
        isComplete = true
//        taskTextView.apply { taskTextView.paintFlags = taskTextView.paintFlags!!.or(Paint.STRIKE_THRU_TEXT_FLAG) }
//
//
//        getChildAt(0).isClickable = false
//        getChildAt(0).visibility = View.INVISIBLE
    }}