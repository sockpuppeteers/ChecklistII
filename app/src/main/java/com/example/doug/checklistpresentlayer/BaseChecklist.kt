package com.example.doug.checklistpresentlayer

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*
import kotlinx.android.synthetic.main.task_settings_deadline_popup.view.*
import kotlinx.android.synthetic.main.task_settings_name_change_popup.view.*
import kotlinx.android.synthetic.main.task_settings_popup.view.*
import kotlinx.android.synthetic.main.task_settings_recursion_popup.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import net.danlew.android.joda.DateUtils

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDate
import kotlin.concurrent.thread
import android.widget.ImageButton
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.db.NULL
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class BaseChecklist : AppCompatActivity(){

    var currentChecklist = Checklist("Your Checklist", 0 )
    var taskFlag = true
    //Flag to see if any popups are present
    var popupPresent = false

    var currentUser = User(1)
    var currentTask: TaskBox? = null
    var currentListofLists = ListofLists("Your CheckLists", "none")

    private lateinit var userLayout: DrawerLayout
    private lateinit var rightnavigationView: NavigationView
    private lateinit var leftnavigationView: NavigationView
    private lateinit var rightmenu: Menu
    private lateinit var leftmenu: Menu
    private lateinit var rightsubMenu: SubMenu
    private lateinit var leftsubMenu: SubMenu
    private lateinit var taskLayout: LinearLayout

    //Intialize things here
    init {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUser = User(intent.getIntExtra("UserID", 0), intent.getStringExtra("UserName"))
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_checklist)
        currentChecklist.listID = intent.getIntExtra("ChecklistID", 0)
        //line below gets the checklist name so that each checklist correctly
        //displays their own tasks and no other checklist's tasks
        currentChecklist.i_name = intent.getStringExtra("ListName")

        setSupportActionBar(findViewById(R.id.toolbar))
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        title = currentChecklist.i_name

        //creates a submenu named user
        rightnavigationView = findViewById(R.id.right_nav_view)
        leftnavigationView = findViewById(R.id.left_nav_view)
        rightmenu = rightnavigationView.menu
        leftmenu = leftnavigationView.menu
        rightsubMenu = rightmenu.addSubMenu(getString(R.string.USER_MENU_TITLE))
        leftsubMenu = leftmenu.addSubMenu(getString(R.string.LIST_MENU_TITLE))
        userLayout = findViewById(R.id.user_drawer_layout)
        taskLayout = findViewById(R.id.TaskLayout)

        val spinner : ProgressBar = findViewById(R.id.progress_bar2)

        //create a database access object
        var db = Database()
        //deleteListDataFile()

        //if there's a local file, populate our list from that
        if (listFileExists()){
            spinner.visibility = View.VISIBLE
            currentChecklist.tasks = getListFromFile()
            //Need to do things with this information TODO
            //Currently this function recognizes if the completed
            //time is 2 days old, but the compared string is never set
            //and nothing is done
            //add each task in currentChecklist to the page
            for (Task in currentChecklist.tasks){
                if (Task.compdatetime != null)
                {
                    val now = LocalDate.now()
                    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                    val dt = formatter.parseDateTime(Task.compdatetime)
                    val dead = dt.plusDays(2)
                    if (now.isEqual(dead.toLocalDate()))
                    {
                        //we don't want the task
                    }
                    else
                    {
                        addTaskFromList(Task)
                    }
                }
                else
                    addTaskFromList(Task)
            }

            println("loaded list from local file")

            //in a new thread, get checklist data from the database to see if any changes
            //have happened since last opened.
            //if there's only one user on the list, don't do anything
            //if (currentChecklist.users.size > 1) {
            GlobalScope.launch {
                /*Right here start up a loading swirly*/

                //disable certain actions while data is being loaded from database
                turnOnButtons()
                turnOffButtons()

                val list = currentChecklist
                list.tasks = db.GetTasks(currentChecklist.listID!!)
                list.users = db.GetUsers(currentChecklist.listID!!)
                list.changes = db.GetChanges(currentChecklist.listID!!)
                currentListofLists.lists = db.GetListofLists(currentUser.Username)

                currentChecklist.users = list.users
                currentChecklist.tasks = list.tasks
                currentChecklist.changes = list.changes

                deleteListDataFile()
                deleteListsDataFile()
                createListFile(currentChecklist)
                createListsFile(currentListofLists)

                this@BaseChecklist.runOnUiThread {
                    rightsubMenu.clear()
                    for ((i, up) in currentChecklist.users.withIndex()) {
                        rightsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, up.Username)
                    }
                    leftsubMenu.clear()
                    for ((i, lol) in currentListofLists.lists.withIndex()) {
                        leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                    }
                    taskLayout.removeAllViews()
                    for (Task in currentChecklist.tasks)
                    {
                        if (Task.compdatetime != null)
                        {
                            val now = LocalDate.now()
                            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                            val dt = formatter.parseDateTime(Task.compdatetime)
                            val dead = dt.plusDays(2)
                            if (now.isEqual(dead.toLocalDate()))
                            {
                                //we don't want the task
                            }
                            else
                            {
                                addTaskFromList(Task)
                            }
                        }
                        else if (Task.name != "")
                            addTaskFromList(Task)
                    }
                    spinner.visibility = View.INVISIBLE
                }
                deleteListDataFile()
                createListFile(currentChecklist)


                /*have a popup or something telling the user that the list has been updated*/
                turnOnButtons()

            }
            //}
        }

        //if no local file exists, populate our list from the database
        else{
            spinner.visibility = View.VISIBLE
            println("loaded list from database")
            var currentTasks = db.GetTasks(intent.getIntExtra("ChecklistID", 0))
            currentChecklist.users = db.GetUsers(intent.getIntExtra("ChecklistID", 0))
            currentChecklist.changes = db.GetChanges(intent.getIntExtra("ChecklistID", 0))
            //Same issue as "Need to do things with this information" TODO
            for (Task in currentTasks)
            {
                if (Task.compdatetime != null)
                {
                    val now = LocalDate.now()
                    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                    val dt = formatter.parseDateTime(Task.compdatetime)
                    val dead = dt.plusDays(2)
                    if (now.isEqual(dead.toLocalDate()))
                    {
                        //we don't want the task
                    }
                    else
                    {
                        addTask(Task)
                    }
                }
                else if (Task.name != "")
                    addTask(Task)

            }

            //create a local file with the data
            GlobalScope.launch {
                createListFile(currentChecklist)
            }
            spinner.visibility = View.INVISIBLE

        }


        //allows the opening and closing of a nav drawer on the right side of the screen.
//        val menuRight = findViewById<View>(R.id.menuRight) as ImageButton
//        menuRight.setOnClickListener {
//            if (userLayout.isDrawerOpen(GravityCompat.END)) {
//                userLayout.closeDrawer(GravityCompat.END)
//            } else {
//                userLayout.openDrawer(GravityCompat.END)
//            }
//        }

        //populates the submenu with the usernames of everyone on the list (stored in mUserList)
        //Menu.FIRST + i gives each a unique ID, used later in the program.
        for ((i, up) in currentChecklist.users.withIndex()) {
            rightsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, up.Username)
        }

        //gets called whenever any item is selected in the user nav menu
        rightnavigationView.setNavigationItemSelectedListener { menuItem ->
            //handles all items in nav drawer that are created at compile time
            if (!onOptionsItemSelected(menuItem))
            {
                //handles all items in nav drawer that are created at run time
                val id = menuItem.itemId - Menu.FIRST
                if (id < currentChecklist.users.size && id >= 0) {
                    val up = currentChecklist.users[id]
                    val tempIntent = Intent(this, UserLogin::class.java).apply {
                        putExtra("id", up.UserID)
                        putExtra("uname", up.Username)
                        putExtra("fname", up.FName)
                        putExtra("lname", up.LName)
                    }
                    startActivity(tempIntent)
                }
            }
            // close drawer when item is tapped
            userLayout.closeDrawers()
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true
        }

        //gets called whenever any item is selected in the user nav menu
        leftnavigationView.setNavigationItemSelectedListener { menuItem ->
            //handles all items in nav drawer that are created at compile time
            if (!onOptionsItemSelected(menuItem))
            {
                spinner.visibility = View.VISIBLE
                GlobalScope.launch {
                    /*Right here start up a loading swirly*/

                    //disable certain actions while data is being loaded from database
                    turnOnButtons()
                    turnOffButtons()
                    val id = menuItem.itemId - Menu.FIRST//come back here
                    val listid = currentListofLists.lists[id].listID
                    currentChecklist.tasks = db.GetTasks(listid!!)
                    currentChecklist.users = db.GetUsers(listid)
                    currentChecklist.changes = db.GetChanges(listid)
                    currentChecklist.i_name = currentListofLists.lists[id].i_name
                    currentListofLists.lists = db.GetListofLists(currentUser.Username)

                    this@BaseChecklist.runOnUiThread {
                        //handles all items in nav drawer that are created at run time
                        val id = menuItem.itemId - Menu.FIRST
                        title = currentChecklist.i_name
                        if (id < currentListofLists.lists.size && id >= 0) {
                            val up = currentListofLists.lists[id]
                            taskLayout.removeAllViews()
                            for (Task in currentChecklist.tasks) {
                                if (Task.compdatetime != null) {
                                    val now = LocalDate.now()
                                    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                                    val dt = formatter.parseDateTime(Task.compdatetime)
                                    val dead = dt.plusDays(2)
                                    if (now.isEqual(dead.toLocalDate())) {
                                        //we don't want the task
                                    } else {
                                        addTaskFromList(Task)
                                    }
                                } else if (Task.name != "")
                                    addTaskFromList(Task)
                            }
                        }
                        spinner.visibility = View.INVISIBLE
                    }
                    turnOnButtons()
                }
            }
            // close drawer when item is tapped
            userLayout.closeDrawers()
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true
        }

        val addButton = findViewById<Button>(R.id.AddTaskButton)
        val checkoffButton = findViewById<Button>(R.id.CheckoffButton)
        val historyButton = findViewById<Button>(R.id.HistoryButton)

        //Creates the click listener for the add button
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

                    if(taskSwitch is CheckBox)
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

                                currentChecklist.completeTask(taskCount, currentUser)
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
                                    "\n\tAdded By: " +  it.changedBy + "\n"
                            kAction.DELETE_TASK -> toAddString = "--- Task Deleted: " + it.taskName +
                                    "\n\tDeleted By: " + it.changedBy + "\n"
                            kAction.COMPLETE_TASK -> toAddString = "--- Task Completed: " + it.taskName +
                                    "\n\tCompleted By: " + it.changedBy + "\n"
                            kAction.CHANGE_TASK_NAME -> toAddString = "--- Task Edited: " + it.taskName +
                                    "\n\tChanged To: " + it.changedTo + "\n\tEdited By: " + it.changedBy + "\n"
                            kAction.CHANGE_TASK_DEADLINE -> toAddString = "--- Deadline Changed: " + it.taskName +
                                    "\n\tChanged to: " + it.changedTo + "\n\tEdited By: " + it.changedBy + "\n"
                            kAction.REMOVE_TASK_DEADLINE -> toAddString + "--- Deadline Removed: " + it.taskName +
                                    "\n\tRemoved By: " + it.changedBy  + "\n"
                            kAction.ADD_USER -> toAddString = "--- User Added: " + it.changedTo +
                                    "\n\tAdded by: " + it.changedBy + "\n"
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

    private fun createSettingsPopup() {
        if (!popupPresent ) {
            popupPresent = true

            val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

            val popupSettingsWindow = PopupWindow(this)

            val taskSettingsLayoutView =
                layoutInflater.inflate(R.layout.task_settings_popup, null)

            var taskCount = 0
            var found = false
            //Checks all current gui elements to see if they are checked
            while (taskCount < currentChecklist.tasks.count() && !found) {
                if(currentChecklist.tasks[taskCount].TaskID == currentTask?.taskID)
                {
                    found = true

                }
                else
                {
                    taskCount++
                }
            }

            popupSettingsWindow.contentView = taskSettingsLayoutView

            /**************
             *   Deadline Button Displays Deadline popup
             ***************/
            taskSettingsLayoutView.DeadlineButton.setOnClickListener {

                popupSettingsWindow.dismiss()
                var curDeadline = LocalDate.now().toString()
                val popupSettingsDeadlineWindow = PopupWindow(this)

                val taskSettingsDeadlineLayoutView =
                    layoutInflater.inflate(R.layout.task_settings_deadline_popup, null)

                var tempString = ""

                popupSettingsDeadlineWindow.contentView = taskSettingsDeadlineLayoutView

                if(currentChecklist.tasks[taskCount].Deadline != null) {
                    tempString =
                        getString(R.string.CURRENT_DEADLINE_TEXT) + " " + currentChecklist.tasks[taskCount].Deadline.toString()

                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = tempString
                }
                else {
                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = getString(R.string.NO_DEADLINE_TEXT)
                }

                taskSettingsDeadlineLayoutView.ClearDeadlineButton.setOnClickListener {
                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = getString(R.string.NO_DEADLINE_TEXT)

                    currentChecklist.removeDeadline(taskCount, currentUser)
                    curDeadline = LocalDate.now().minusDays(1).toString()
                }

                taskSettingsDeadlineLayoutView.DeadlineCalendarView.setOnDateChangeListener{_, year, month, day ->
                    tempString =
                        getString(R.string.CURRENT_DEADLINE_TEXT) + " " + year + "-" + month + "-" + day

                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = tempString

                    curDeadline = "$year-$month-$day"
                }

                taskSettingsDeadlineLayoutView.closeDeadlineButton.setOnClickListener{

                    popupSettingsDeadlineWindow.dismiss()

                    popupPresent = false
                }

                popupSettingsDeadlineWindow.setOnDismissListener {
                    if (curDeadline != LocalDate.now().minusDays(1).toString())
                        currentChecklist.changeTaskDeadline(taskCount, currentUser, curDeadline)
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

                taskSettingsRecurringLayoutView.RecursionSwitch.setOnClickListener {
                    currentTask?.setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)
                }

                taskSettingsRecurringLayoutView.CurrentDaysTextView.text = calcTempStringDays(taskCount)

                taskSettingsRecurringLayoutView.CurrentTimeTextView.text = calcTempStringTime(taskCount)

                popupSettingsRecurringWindow.contentView = taskSettingsRecurringLayoutView

                popupSettingsRecurringWindow.setOnDismissListener {

                    currentTask?.setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)

                    popupPresent = false

                }
                taskSettingsRecurringLayoutView.RecursionSwitch.setOnClickListener {
                    currentTask?.setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)
                    currentChecklist.setTaskRecursion(taskCount, currentUser,
                        taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)
                }

                taskSettingsRecurringLayoutView.CloseRecurringButton.setOnClickListener{

                    currentTask?.setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)

                    popupSettingsRecurringWindow.dismiss()

                    popupPresent = false
                }

                //********************************************************************
                //Format for recurring date string is Day-Day-Day-.... ex. Mon-Tue-Fri
                //
                //Format for recurring time string is hour:minute AM\PM
                //********************************************************************
                taskSettingsRecurringLayoutView.SaveRecurringSettingsButton.setOnClickListener {
                    var dateString = ""

                    if(taskSettingsRecurringLayoutView.SundaySwitch.isChecked)
                        dateString += "Sun-"

                    if(taskSettingsRecurringLayoutView.MondaySwitch.isChecked)
                        dateString += "Mon-"

                    if(taskSettingsRecurringLayoutView.TuesdaySwitch.isChecked)
                        dateString += "Tue-"

                    if(taskSettingsRecurringLayoutView.WednesdaySwitch.isChecked)
                        dateString += "Wed-"

                    if(taskSettingsRecurringLayoutView.ThursdaySwitch.isChecked)
                        dateString += "Thu-"

                    if(taskSettingsRecurringLayoutView.FridaySwitch.isChecked)
                        dateString += "Fri-"

                    if(taskSettingsRecurringLayoutView.SaturdaySwitch.isChecked)
                        dateString += "Sat-"

                    if(dateString != currentChecklist.tasks[taskCount].recurringDays)
                        currentChecklist.updateTaskRecurringDays(taskCount, currentUser, dateString)

                    var timeString =
                        taskSettingsRecurringLayoutView.HourSpinner.selectedItem.toString() +
                                ":" + taskSettingsRecurringLayoutView.MinuteSpinner.selectedItem.toString() +
                                " " + taskSettingsRecurringLayoutView.AmPmSpinner.selectedItem.toString()

                    if(timeString != currentChecklist.tasks[taskCount].recurringTime)
                        currentChecklist.updateTaskRecurringTime(taskCount, currentUser, timeString)

                    taskSettingsRecurringLayoutView.CurrentDaysTextView.text = calcTempStringDays(taskCount)

                    taskSettingsRecurringLayoutView.CurrentTimeTextView.text = calcTempStringTime(taskCount)
                }

                taskSettingsRecurringLayoutView.RecursionSwitch.isChecked =
                    currentTask?.checkReccurring() != null && currentTask?.checkReccurring() == true


                popupSettingsRecurringWindow.setOnDismissListener {
                    popupPresent = false
                }

                popupSettingsRecurringWindow.isFocusable = true

                popupSettingsRecurringWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
            }

            taskSettingsLayoutView.ChangeNameSettingsButton.setOnClickListener {

                popupSettingsWindow.dismiss()

                val popupSettingsChangeNameWindow = PopupWindow(this)

                val taskSettingsChangeNameLayoutView =
                    layoutInflater.inflate(R.layout.task_settings_name_change_popup, null)

                popupSettingsChangeNameWindow.contentView = taskSettingsChangeNameLayoutView

                taskSettingsChangeNameLayoutView.ChangeNameButton.setOnClickListener {

                    val newName = taskSettingsChangeNameLayoutView.NewNameText.text.toString()

                    currentTask?.ChangeName(newName)

                    currentChecklist.changeTaskName(taskCount, currentUser, newName)

                    popupPresent = false

                    popupSettingsChangeNameWindow.dismiss()
                }

                popupSettingsChangeNameWindow.setOnDismissListener {
                    popupPresent = false
                }

                taskSettingsChangeNameLayoutView.ChangeNameCancelButton.setOnClickListener {
                    popupPresent = false

                    popupSettingsChangeNameWindow.dismiss()
                }

                popupSettingsChangeNameWindow.isFocusable = true

                popupSettingsChangeNameWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
            }

            taskSettingsLayoutView.CloseButton.setOnClickListener {
                popupSettingsWindow.dismiss()
                popupPresent = false
            }

            popupSettingsWindow.setOnDismissListener {
                popupPresent = false
            }

            taskSettingsLayoutView.taskNameView.text = currentTask?.getTaskText()

            popupSettingsWindow.isFocusable = true

            popupSettingsWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
        }
    }

    private fun calcTempStringDays(index: Int) = when(currentChecklist.tasks[index].recurringDays != null) {
        true -> getString(R.string.CURRENT_RECURRING_DAYS_TEXT) +
                " " + currentChecklist.tasks[index].recurringDays
        false -> "No current recurring days"
    }

    private fun calcTempStringTime(index: Int) = when(currentChecklist.tasks[index].recurringTime != null){
        true -> getString(R.string.CURRENT_RECURRING_TIME_TEXT) +
                " " + currentChecklist.tasks[index].recurringTime
        false -> "No current recurring time"
    }

    fun createNewTask(TaskText: String, IsReaccuring: Boolean, taskID: Int?) {
        var new_task_box = TaskBox(
            this,
            TaskText
        )

        new_task_box.taskID = taskID

        if(IsReaccuring)
            new_task_box.setRecurringIfNotComplete(IsReaccuring)

        val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

        //Adds the task to the checklist
        currentChecklist.createTask(TaskText, null, currentUser, null, currentChecklist.listID!!)

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
                        currentChecklist.deleteTask(i, currentUser)

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

            if(!popupPresent && taskFlag) {

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

        new_task_box.taskID = task.TaskID

        if(task.isRecurring == true)
            new_task_box.setRecurringIfNotComplete(true)
        if (task.compdatetime != null)
            new_task_box.completeTask()

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
                        currentChecklist.deleteTask(i, currentUser)
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

        new_task_box.taskID = task.TaskID

        if(task.isRecurring == true)
            new_task_box.setRecurringIfNotComplete(true)
        if (task.compdatetime != null)
            new_task_box.completeTask()

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
                        currentChecklist.deleteTask(i, currentUser)

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

    fun createListFile(list: Checklist) {
        //convert list to a JSON string
        val gson = Gson()
        val userJson = gson.toJson(list.tasks)

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

    fun createListsFile(lists: ListofLists) {
        //convert lists to a JSON string
        val gson = Gson()
        val userJson = gson.toJson(lists.lists)

        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "LISTS"
        val directory = context.filesDir

        //write the file LISTS to local directory
        val file = File(directory, filename)
        FileOutputStream(file).use {
            it.write(userJson.toByteArray())
        }
    }

    fun listsFileExists() : Boolean {
        return File(applicationContext.filesDir, "LISTS").exists()
    }

    //we don't have to check if the file exists in this function
    //because we call listFileExists() before calling this
    //however, we might need some other error checking in here
    fun getListsFromFile() : MutableList<ListClass> {
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "LISTS"
        val directory = context.filesDir

        //read from LISTS and store it as a string
        val file = File(directory, filename)
        val fileData = FileInputStream(file).bufferedReader().use { it.readText() }

        //create a MutableList<ListClass> object based on the JSON from the file
        val gson = Gson()
        return gson.fromJson(fileData, object : TypeToken<MutableList<ListClass>>() {}.type)
    }

    fun deleteUserDataFile(){
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "USERDATA"
        val directory = context.filesDir

        //delete the USERDATA file
        File(directory, filename).delete()
    }

    //deletes the file that contains list of lists data
    fun deleteListsDataFile(){
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "LISTS"
        val directory = context.filesDir

        //delete the LISTS file
        File(directory, filename).delete()
    }

    //deletes the file with the given name.
    //this should be an individual checklist's file, which is filled
    //with task/change data
    fun deleteListDataFile(filename: String){
        //context will give us access to our local files directory
        var context = applicationContext

        val directory = context.filesDir

        //delete the file
        File(directory, filename).delete()
    }

    //we don't have to check if the file exists in this function
    //because we call listFileExists() before calling this
    //however, we might need some other error checking in here
    fun getListFromFile() : MutableList<Task> {
        //context will give us access to our local files directory
        val context = applicationContext

        val filename = currentChecklist.i_name
        val directory = context.filesDir

        //read from the file and store it as a string
        val file = File(directory, filename)
        val fileData = FileInputStream(file).bufferedReader().use { it.readText() }

        //create a Checklist object based on the JSON from the file
        val gson = Gson()
        return gson.fromJson(fileData, object : TypeToken<MutableList<Task>>() {}.type)
    }

    fun deleteListDataFile(){
        //context will give us access to our local files directory
        val context = applicationContext

        val filename = currentChecklist.i_name
        val directory = context.filesDir

        //delete the file
        File(directory, filename).delete()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //handles all nav drawer activity that was added at run time.
        return when (item.itemId) {
            android.R.id.home -> {
                userLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.users_drawer -> {
                if (userLayout.isDrawerOpen(GravityCompat.END)) {
                    userLayout.closeDrawer(GravityCompat.END)
                } else {
                    userLayout.openDrawer(GravityCompat.END)
                }
                //userLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the options menu from XML
        val inflater = menuInflater
        val db = Database()
        inflater.inflate(R.menu.options_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        if(searchItem != null){
            val searchView = searchItem.actionView as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val u = db.GetUser(query as String)
                    if (u.UserID != -1) {
                        db.AddUserToList(u.UserID as Int, currentChecklist.listID as Int)
                        rightsubMenu.add(0, Menu.FIRST + 7, Menu.FIRST, u.Username)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {

//                    displayList.clear()
//                    if(newText!!.isNotEmpty()){
//                        val search = newText.toLowerCase()
//                        countries.forEach {
//                            if(it.toLowerCase().contains(search)){
//                                displayList.add(it)
//                            }
//                        }
//                    }else{
//                        displayList.addAll(countries)
//                    }
//                    country_list.adapter.notifyDataSetChanged()
                    return true
                }

            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun turnOffButtons() {
        val turnOff: Button = findViewById(R.id.AddTaskButton)
        turnOff.isClickable = false
        val turnOff2 : Button = findViewById(R.id.CheckoffButton)
        turnOff2.isClickable = false
        taskFlag = false
    }

    private fun turnOnButtons() {

        var turnOn : Button = findViewById(R.id.AddTaskButton)
        turnOn.isClickable = true
        turnOn = findViewById(R.id.CheckoffButton)
        turnOn.isClickable = true
        taskFlag = true
    }
}
