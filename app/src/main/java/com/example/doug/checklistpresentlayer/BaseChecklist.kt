package com.example.doug.checklistpresentlayer

import android.content.Intent
import khttp.*
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*
import kotlinx.android.synthetic.main.task_settings_deadline_popup.view.*
import kotlinx.android.synthetic.main.task_settings_popup.view.*
import kotlinx.android.synthetic.main.task_settings_recursion_popup.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread


/********************************************
 *TO DO: Move listener assignments to functions
 ********************************************/
class BaseChecklist : AppCompatActivity(){

    var currentChecklist = Checklist("Your Checklist", 0 )

    //Flag to see if any popups are present
    var popupPresent = false

    var currentTask: TaskBox? = null

    //Intialize things here
    init {

    }

    private fun createSettingsPopup() {
        if (!popupPresent ) {
            popupPresent = true

            val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

            val popupSettingsWindow = PopupWindow(this)

            val taskSettingsLayoutView =
                layoutInflater.inflate(R.layout.task_settings_popup, null)

            popupSettingsWindow.contentView = taskSettingsLayoutView

            /**************
             *   Deadline Button Displays Deadline popup
             ***************/
            taskSettingsLayoutView.DeadlineButton.setOnClickListener {

                popupSettingsWindow.dismiss()

                val popupSettingsDeadlineWindow = PopupWindow(this)

                val taskSettingsDeadlineLayoutView =
                    layoutInflater.inflate(R.layout.task_settings_deadline_popup, null)

                popupSettingsDeadlineWindow.contentView = taskSettingsDeadlineLayoutView

                taskSettingsDeadlineLayoutView.closeDeadlineButton.setOnClickListener{

                    popupSettingsDeadlineWindow.dismiss()

                    popupPresent = false
                }

                popupSettingsDeadlineWindow.isFocusable = true

                popupSettingsDeadlineWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
            }

            /**************
             *   Recursion Button Displays Task Recursion popup
             ***************/
            taskSettingsLayoutView.RecursionButton.setOnClickListener {

                popupSettingsWindow.dismiss()

                val popupSettingsRecurringWindow = PopupWindow(this)

                val taskSettingsRecurringLayoutView =
                    layoutInflater.inflate(R.layout.task_settings_recursion_popup, null)

                popupSettingsRecurringWindow.contentView = taskSettingsRecurringLayoutView

                taskSettingsRecurringLayoutView.CloseRecurringButton.setOnClickListener{

                    popupSettingsRecurringWindow.dismiss()

                    popupPresent = false
                }

                taskSettingsRecurringLayoutView.RecursionSwitch.isChecked =
                    currentTask?.checkReccurring() != null && currentTask?.checkReccurring() == true

                taskSettingsRecurringLayoutView.RecursionSwitch.setOnClickListener {
                    currentTask?.toggleReccurringIfNotComplete()
                }

                popupSettingsRecurringWindow.isFocusable = true

                popupSettingsRecurringWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
            }

            taskSettingsLayoutView.CloseButton.setOnClickListener {
                popupSettingsWindow.dismiss()
                popupPresent = false
            }

            taskSettingsLayoutView.taskNameView.text = currentTask?.getTaskText()

            popupSettingsWindow.isFocusable = true

            popupSettingsWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
        }
    }

    fun createNewTask(TaskText: String, IsReaccuring: Boolean, taskID: Int?) {
        var new_task_box = TaskBox(
            this,
            TaskText
        )

        if(IsReaccuring)
            new_task_box.toggleReccurringIfNotComplete()

        val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

        //Adds the task to the checklist
        currentChecklist.createTask(TaskText, null, User(intent.getIntExtra("UserID", 0)), null, currentChecklist.listID!!)

        //rebuild the local file with the updated checklist
        GlobalScope.launch {
            deleteListDataFile()
            createListFile(currentChecklist)
        }

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
                        //remove the task from the list, and delete it from the database
                        currentChecklist.deleteTask(i, User(1))

                        //update the local file
                        GlobalScope.launch {
                            deleteListDataFile()
                            createListFile(currentChecklist)
                        }
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

    fun addTask(task: Task) {
        var new_task_box = TaskBox(
            this,
            task.name
        )

        if(task.isRecurring == true)
            new_task_box.toggleReccurringIfNotComplete()

        val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

        //Adds the task to the checklist
        currentChecklist.tasks.add(task)

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

    fun addTaskFromList(task: Task) {
        var new_task_box = TaskBox(
            this,
            task.name
        )

        if(task.isRecurring == true)
            new_task_box.toggleReccurringIfNotComplete()

        val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

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
                        currentChecklist.deleteTask(i, User(1))

                        //remake the local file
                        GlobalScope.launch {
                            deleteListDataFile()
                            createListFile(currentChecklist)
                        }
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
        currentChecklist.listID = intent.getIntExtra("ChecklistID", 0)

        //if there's a local file, populate our list from that
        if (listFileExists()){
            //deleteListDataFile()
            currentChecklist = getListFromFile()

            //add each task in currentChecklist to the page
            for (Task in currentChecklist.tasks){
                addTaskFromList(Task)
            }
            println("loaded list from local file")
        }

        //if no local file exists, populate our list from the database
        else{
            println("loaded list from database")
            var db = Database()
            var currentTasks = db.GetTasks(intent.getIntExtra("ChecklistID", 0))

            for (Task in currentTasks)
            {
                if (Task.name != "")
                    addTask(Task)
            }

            //create a local file with the data
            GlobalScope.launch {
                createListFile(currentChecklist)
            }
        }

        val addButton = findViewById<Button>(R.id.AddTaskButton)
        val checkoffButton = findViewById<Button>(R.id.CheckoffButton)
        val historyButton = findViewById<Button>(R.id.HistoryButton)

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
                            createNewTask(popup_edittext.text.toString(), false, 0/*needs to be something later*/)
                            //currentChecklist.createTask(popup_edittext.text.toString(),
                            //   "none", User(intent.getIntExtra("UserID", 0)))
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
                                    createNewTask(currentChild.getTaskText(), true, 0/*needs to be something later*/)
                                    //currentChecklist.createTask(currentChild.getTaskText(), "enable Later", User(1))
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
    }

    fun createListFile(list: Checklist) {
        //convert list to a JSON string
        val gson = Gson()
        val userJson = gson.toJson(list)

        //context will give us access to our local files directory
        var context = applicationContext

        val filename = list.i_name
        val directory = context.filesDir

        //write the file to local directory
        //the filename will be the name of the list
        val file = File(directory, filename)
        FileOutputStream(file).use {
            it.write(userJson.toByteArray())
        }
    }

    fun listFileExists() : Boolean {
        return File(applicationContext.filesDir, currentChecklist.i_name).exists()
    }

    //we don't have to check if the file exists in this function
    //because we call listFileExists() before calling this
    //however, we might need some other error checking in here
    fun getListFromFile() : Checklist {
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = currentChecklist.i_name
        val directory = context.filesDir

        //read from the file and store it as a string
        val file = File(directory, filename)
        val fileData = FileInputStream(file).bufferedReader().use { it.readText() }

        //create a Checklist object based on the JSON from the file
        val gson = Gson()
        return gson.fromJson(fileData, Checklist::class.java)
    }

    fun deleteListDataFile(){
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = currentChecklist.i_name
        val directory = context.filesDir

        //delete the file
        File(directory, filename).delete()
    }
}
