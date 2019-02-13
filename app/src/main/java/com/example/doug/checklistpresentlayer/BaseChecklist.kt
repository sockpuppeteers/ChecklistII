package com.example.doug.checklistpresentlayer

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*


/********************************************
 *TO DO: Move listener assignments to functions
 ********************************************/
class BaseChecklist : AppCompatActivity(){

    var inEdit = false
    var currentChecklist = Checklist("Your Checklist")

    //Flag to see if any popups are present
    var popupPresent = false

    var currentTask: TaskBox? = null

    //Intialize things here
    init {

    }

    /********************************************
     *Purpose: Click Listener for the edit button
     *
     * DO NOT USE
     ********************************************/
    val edit_listener = View.OnClickListener {

        /*var taskCount = TaskLayout.childCount - 1

        while (taskCount >= 0)
        {
            val currentChild = TaskLayout.getChildAt(taskCount)

            if(currentChild is TaskBox)
            {
                val taskSwitch = currentChild.getChildAt(1)

                if(taskSwitch is Switch)
                {
                    if(taskSwitch.isChecked)
                    {
                        TaskLayout.removeView(TaskLayout.getChildAt(taskCount))
                    }
                }
            }

            taskCount--
        }
        */
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_checklist)

        val addButton = findViewById<Button>(R.id.AddTaskButton)
        val checkoffButton = findViewById<Button>(R.id.CheckoffButton)
        val historyButton = findViewById<Button>(R.id.HistoryButton)
        val editButton = findViewById<Button>(R.id.EditTaskButton)

        //Creates the clikc listener for the add button
        val addListener = View.OnClickListener {

                //If there is not a popup already [resent
            if(!popupPresent) {

                //Get the view containing all the tasks
                val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

                val popupWindow = PopupWindow(this)
                //Create a view that is of the popup_layout in resources
                val popupView = layoutInflater.inflate(R.layout.popup_layout, null)
                //Sets the content of the popup to the popup_layout
                popupWindow.contentView = popupView
                //Retrieves the acceptButton from the popup
                val acceptButton = popupView.PopupMainView.AcceptButton

                //Creates and adds the on click action to the add button
                acceptButton.setOnClickListener{

                        val popup_edittext = popupView.PopupMainView.PopupEditText

                        val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)
                        //Retrieves the name of the task if the name is long enough
                        if (popup_edittext.text.toString().length >= 1) {
                            var new_task_box = TaskBox(
                                this,
                                popup_edittext.text.toString()
                            )
                            //Adds the task to the checklist
                            currentChecklist.createTask(popup_edittext.text.toString(), "enable Later", User(1))

                            //Set dismiss listener
                            popupWindow.setOnDismissListener {
                                popupPresent = false
                            }
                            //Dismisses the popup
                            popupWindow.dismiss()

                            val popupFunctionWindow = PopupWindow(this)

                            val taskFunctionLayoutView =
                                layoutInflater.inflate(R.layout.task_functions_layout, null)

                            popupFunctionWindow.contentView = taskFunctionLayoutView

                            val DeleteButton = taskFunctionLayoutView.FunctionDeleteButton

                            val CloseButton = taskFunctionLayoutView.FunctionCloseButton

                            val CloseListener = View.OnClickListener {
                                popupFunctionWindow.dismiss()

                                popupPresent = false
                            }
                            //Sets the delete button to remove the task
                            val DeleteListener = View.OnClickListener {
                                for(i in TaskLayout.childCount downTo 0 step 1)
                                {
                                    val tempChild = TaskLayout.getChildAt(i)
                                    if(tempChild is TaskBox)
                                    {
                                        if(tempChild.taskText == currentTask?.taskText)
                                        {
                                            TaskLayout.removeView(TaskLayout.getChildAt(i))
                                            currentChecklist.deleteTask(i, User(1));
                                        }
                                    }
                                }

                                popupFunctionWindow.dismiss()

                                popupPresent = false
                            }

                            popupFunctionWindow.contentView = taskFunctionLayoutView

                            DeleteButton.setOnClickListener(DeleteListener)

                            CloseButton.setOnClickListener(CloseListener)

                            popupFunctionWindow.setOnDismissListener {
                                PopupWindow.OnDismissListener {
                                    popupPresent = false
                                }
                            }

                            //Sets the on lick listener for the new task gui element
                            new_task_box.setOnClickListener(View.OnClickListener {

                                if(!popupPresent) {

                                    popupPresent = true

                                    popupFunctionWindow.isFocusable()

                                    popupFunctionWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                                    for(i in TaskLayout.childCount downTo 0 step 1)
                                    {
                                        val tempChild = TaskLayout.getChildAt(i)
                                        if(tempChild is TaskBox)
                                        {
                                            if(tempChild.taskText == new_task_box.taskText) {
                                                currentTask = tempChild
                                            }
                                        }
                                    }
                                }
                            })

                            taskLayout.addView(new_task_box)
                        }
                }
                //Set cancel button to dismiss the popup
                val cancelButton = popupView.PopupMainView.CancelButton

                cancelButton.setOnClickListener(View.OnClickListener {

                    popupWindow.dismiss()

                })
                //Have the popup clean up items when dismissed
                popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
                    val popupEdittext = popupView.PopupMainView.PopupEditText

                    popupEdittext.text.clear()

                    popupPresent = false
                })

                popupWindow.isFocusable = true

                popupWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                popupPresent = true

            }
        }

        addButton.setOnClickListener(addListener)
        //Create the click listener for the checkoff button
        val checkoffListener = View.OnClickListener {

            var taskCount = TaskLayout.childCount - 1
            //Checks all current gui elements to see if they are checked
            while (taskCount >= 0)
            {
                val currentChild = TaskLayout.getChildAt(taskCount)

                if(currentChild is TaskBox)
                {
                    val taskSwitch = currentChild.getChildAt(1)

                    if(taskSwitch is Switch)
                    {
                        if(taskSwitch.isChecked)
                        {
                            var found = false

                            TaskLayout.removeView(TaskLayout.getChildAt(taskCount))

                            val taskIterator = currentChecklist.tasks.iterator()

                            currentChecklist.completeTask(taskCount, User(1))
                        }
                    }
                }

                taskCount--
            }
        }

        checkoffButton.setOnClickListener(checkoffListener)

        //Set history button's click listener

        val historyListener = View.OnClickListener {
            //Toast.makeText(this, "General Kenobi!", Toast.LENGTH_SHORT).show()

            if(!popupPresent) {

                val mainViewHistory = findViewById<ScrollView>(R.id.TaskScrollView)

                val popupWindowHistory = PopupWindow(this)

                val popupViewHistory = layoutInflater.inflate(R.layout.history_popup, null)

                popupWindowHistory.contentView = popupViewHistory


                val cancelListener = View.OnClickListener {
                    popupWindowHistory.dismiss()
                }

                val dismissListener = PopupWindow.OnDismissListener {
                    popupPresent = false
                }

                popupWindowHistory.setOnDismissListener(dismissListener)

                popupViewHistory.HistoryCloseButton.setOnClickListener(cancelListener)

                popupPresent = true

                popupWindowHistory.isFocusable = true

                val historyIterator = currentChecklist.changes.iterator()

                val historyLayout = popupViewHistory.HistoryLinearLayout

                //Check to see if not changes have happened
                //Displays a message for each change that has occurred
                if(currentChecklist.changes.isEmpty()) {
                    val checklistChangeTextView = TextView(this)

                    var toAddString = "No Changes in this checklist!"

                    checklistChangeTextView.text = toAddString

                    checklistChangeTextView.setTextColor(Color.WHITE)

                    checklistChangeTextView.textSize = 30f
                    checklistChangeTextView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    historyLayout.addView(checklistChangeTextView)
                }
                else {
                    historyIterator.forEach {

                        val checklistChangeTextView = TextView(this)

                        var toAddString = "Default"

                        when(it.changeType) {

                            kAction.CREATE_TASK -> toAddString = "--- Task Added: " + it.taskName +
                                    "\n    Added By: Current User\n"
                            kAction.DELETE_TASK -> toAddString = "--- Task Deleted: " + it.taskName +
                                    "\n    Deleted By: Current User\n"
                            kAction.COMPLETE_TASK -> toAddString = "--- Task Completed: " + it.taskName +
                                    "\n    Completed By: Current User\n"
                        }

                        checklistChangeTextView.text = toAddString

                        checklistChangeTextView.setTextColor(Color.WHITE)

                        checklistChangeTextView.textSize = 20f
                        checklistChangeTextView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        historyLayout.addView(checklistChangeTextView)
                    }
                }

                popupWindowHistory.showAtLocation(mainViewHistory, Gravity.CENTER, 0, 0)
            }
        }

        historyButton.setOnClickListener(historyListener)

        editButton.setOnClickListener(edit_listener)

    }
}
