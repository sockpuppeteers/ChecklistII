package com.example.doug.checklistpresentlayer

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*
import kotlinx.android.synthetic.main.task_settings_popup.view.*


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

    private fun createSettingsPopup() {
        if (!popupPresent ) {
            popupPresent = true

            val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

            val popupSettingsWindow = PopupWindow(this)

            val taskSettingsLayoutView =
                layoutInflater.inflate(R.layout.task_settings_popup, null)

            popupSettingsWindow.contentView = taskSettingsLayoutView

            //This is the weirdest statement I have ever written but it yells at me otherwise
            taskSettingsLayoutView.RecursionSwitch.isChecked =
                currentTask?.checkReccurring() != null && currentTask?.checkReccurring() == true

            taskSettingsLayoutView.RecursionSwitch.setOnClickListener {
                currentTask?.toggleReccurringIfNotComplete()
            }

            taskSettingsLayoutView.CloseButton.setOnClickListener {
                popupSettingsWindow.dismiss()
            }

            taskSettingsLayoutView.taskNameView.text = currentTask?.getTaskText()

            taskSettingsLayoutView.ApplyButton.setOnClickListener {

                for(i in TaskLayout.childCount downTo 0 step 1)
                {
                    val tempChild = TaskLayout.getChildAt(i)
                    if(tempChild is TaskBox)
                    {
                        if(tempChild == currentTask)
                        {
                            var newDeadline = taskSettingsLayoutView.DaySpinner.selectedItem.toString()
                            newDeadline += "/" + taskSettingsLayoutView.MonthSpinner.selectedItem.toString()
                            newDeadline += "/" + taskSettingsLayoutView.YearSpinner.selectedItem.toString()
                            newDeadline += " " + taskSettingsLayoutView.HourSpinner.selectedItem.toString()
                            newDeadline += ":" + taskSettingsLayoutView.MinuteSpinner.selectedItem.toString()
                            newDeadline += " " + taskSettingsLayoutView.AmPmSpinner.selectedItem.toString()

                            currentTask?.setDeadline(newDeadline)

                            currentChecklist.changeTaskDeadline(i, User(1), newDeadline)

                            val currentDeadlineText = getString(R.string.CURRENT_DEADLINE_TEXT) + "\n" + newDeadline

                            taskSettingsLayoutView.DeadlineDisplayText.text = currentDeadlineText
                        }
                    }
                }
            }

            taskSettingsLayoutView.ClearDeadline.setOnClickListener {
                if(currentTask?.getDeadline() != "No Deadline") {
                    for (i in TaskLayout.childCount downTo 0 step 1) {
                        val tempChild = TaskLayout.getChildAt(i)
                        if (tempChild is TaskBox) {
                            if (tempChild == currentTask) {
                                var newDeadline = "No Deadline"

                                currentTask?.setDeadline(newDeadline)

                                currentChecklist.removeDeadline(i, User(1))

                                val currentDeadlineText = getString(R.string.CURRENT_DEADLINE_TEXT) + "\n" + newDeadline

                                taskSettingsLayoutView.DeadlineDisplayText.text = currentDeadlineText
                            }
                        }
                    }
                }
            }

            val currentDeadlineText = getString(R.string.CURRENT_DEADLINE_TEXT) + "\n" + currentTask?.getDeadline()

            taskSettingsLayoutView.DeadlineDisplayText.text = currentDeadlineText

            popupSettingsWindow.setOnDismissListener {
                popupPresent = false
            }

            popupSettingsWindow.isFocusable = true

            popupSettingsWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
        }
    }

    fun createNewTask(TaskText: String, IsReaccuring: Boolean) {
        var new_task_box = TaskBox(
            this,
            TaskText
        )

        if(IsReaccuring)
            new_task_box.toggleReccurringIfNotComplete()

        val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

        //Adds the task to the checklist
        currentChecklist.createTask(TaskText, "enable Later", User(1))

        val popupFunctionWindow = PopupWindow(this)

        val taskFunctionLayoutView =
            layoutInflater.inflate(R.layout.task_functions_layout, null)

        taskFunctionLayoutView.FunctionCloseButton.setOnClickListener {
            popupFunctionWindow.dismiss()

            popupPresent = false
        }

        taskFunctionLayoutView.FunctionSettingsButton.setOnClickListener {
            popupFunctionWindow.dismiss()

            popupPresent = false

            createSettingsPopup()
        }

        //Sets the delete button to remove the task
        taskFunctionLayoutView.FunctionDeleteButton.setOnClickListener {
            for(i in TaskLayout.childCount downTo 0 step 1)
            {
                val tempChild = TaskLayout.getChildAt(i)
                if(tempChild is TaskBox)
                {
                    if(tempChild == currentTask)
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

        popupFunctionWindow.setOnDismissListener {
            PopupWindow.OnDismissListener {
                popupPresent = false
            }
        }

        //Sets the on lick listener for the new task gui element
        new_task_box.setOnClickListener{

            if(!popupPresent) {

                popupPresent = true

                popupFunctionWindow.isFocusable()

                popupFunctionWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                for(i in TaskLayout.childCount downTo 0 step 1)
                {
                    val tempChild = TaskLayout.getChildAt(i)
                    if(tempChild is TaskBox)
                    {
                        if(tempChild == new_task_box) {
                            currentTask = tempChild
                        }
                    }
                }
            }
        }

        val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)

        taskLayout.addView(new_task_box)
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

                        //Retrieves the name of the task if the name is long enough
                        if (popup_edittext.text.toString().length >= 1) {
                            createNewTask(popup_edittext.text.toString(), false)
                        }

                    //Set dismiss listener
                    popupWindow.setOnDismissListener {
                        popupPresent = false
                    }
                    //Dismisses the popup
                    popupWindow.dismiss()
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
                            if(!currentChild.checkCompletion()) {
                                if (currentChild.checkReccurring()) {
                                    createNewTask(currentChild.getTaskText(), true)
                                    currentChecklist.createTask(currentChild.getTaskText(), "enable Later", User(1))
                                }

                                currentChild.completeTask()

                                //TaskLayout.removeView(TaskLayout.getChildAt(taskCount))

                                currentChecklist.completeTask(taskCount, User(1))
                            }
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
                                    "\n\tAdded By: Current User\n"
                            kAction.DELETE_TASK -> toAddString = "--- Task Deleted: " + it.taskName +
                                    "\n\tDeleted By: Current User\n"
                            kAction.COMPLETE_TASK -> toAddString = "--- Task Completed: " + it.taskName +
                                    "\n\tCompleted By: Current User\n"
                            kAction.CHANGE_TASK_DEADLINE -> toAddString = "---Deadline Changed On: \n\t\t" + it.taskName +
                                    "\n\tBy: Current User\n"
                            kAction.REMOVE_TASK_DEADLINE -> toAddString = "---Deadline Removed On: \n\t\t" + it.taskName +
                                    "\n\tBy: Current User\n"
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
