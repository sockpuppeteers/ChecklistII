package com.example.doug.checklistpresentlayer

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent.getActivity
import android.app.SearchManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils.split
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
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
import org.jetbrains.anko.searchView
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class BaseChecklist : AppCompatActivity(){

    var currentChecklist = Checklist("Your Checklist", 0 )
    //var lastChecklist: Deque<Checklist> =
    var taskFlag = true
    var deleteFlag = false
    var editFlag = false
    //Flag to see if any popups are present
    var popupPresent = false

    var currentUser = User(1)
    var numUser = 0
    var currentTask: ChecklistViewModel = ChecklistViewModel("")
    var currentListofLists = ListofLists("Your CheckLists", "none")
    var currentListView: ArrayList<ChecklistViewModel> = ArrayList()

    private lateinit var userLayout: DrawerLayout
    private lateinit var rightnavigationView: NavigationView
    private lateinit var leftnavigationView: NavigationView
    private lateinit var userButton: ClipData.Item
    private lateinit var rightmenu: Menu
    private lateinit var leftmenu: Menu
    private lateinit var rightsubMenu: SubMenu
    private lateinit var leftsubMenu: SubMenu
    private lateinit var taskLayout: LinearLayout
    private lateinit var adapter: ChecklistAdapter
    private lateinit var recyclerView: RecyclerView

    //Intialize things here
    init {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUser = User(
            intent.getIntExtra("UserID", 0), intent.getStringExtra("UserName"),
            intent.getStringExtra("fname"), intent.getStringExtra("lname")
        )
        //sets the orientation to portrait permanently
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_checklist)
        currentChecklist.listID = intent.getIntExtra("ChecklistID", 0)
        //line below gets the checklist name so that each checklist correctly
        //displays their own tasks and no other checklist's tasks
        currentChecklist.i_name = intent.getStringExtra("ListName")
        currentListofLists.uID = currentUser.UserID as Int

        //creates the top bar and changes the left most icon to the left nav drawers icon
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        title = currentChecklist.i_name


        //big mess of initializer.
        rightnavigationView = findViewById(R.id.right_nav_view)
        leftnavigationView = findViewById(R.id.left_nav_view)
        rightmenu = rightnavigationView.menu
        leftmenu = leftnavigationView.menu
        rightsubMenu = rightmenu.addSubMenu(getString(R.string.USER_MENU_TITLE))
        leftsubMenu = leftmenu.addSubMenu(getString(R.string.LIST_MENU_TITLE))
        userLayout = findViewById(R.id.user_drawer_layout)
        recyclerView = findViewById(R.id.checklist_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val addButton = findViewById<Button>(R.id.AddTaskButton)
        val checkoffButton = findViewById<Button>(R.id.CheckoffButton)
        val historyButton = findViewById<Button>(R.id.HistoryButton)

        //loads the animation for recyclerview. The cascading list items one.
        val resId = R.anim.layout_animation_fall_down
        val animation = AnimationUtils.loadLayoutAnimation(this, resId)
        recyclerView.layoutAnimation = animation

        val spinner: ProgressBar = findViewById(R.id.progress_bar2)

        //create a database access object
        var db = Database()
        //deleteListDataFile()

        //if there's no list then display a message
        if (currentChecklist.listID == -1)
        {
            title = ""

            //displays a message
            var message = ChecklistViewModel("This list is empty. Select or create a new one.")
            message.isMessage = true
            currentListView.add(message)
            //changes the data set in the adapter, changing whats in recyclerview
            adapter = ChecklistAdapter(
                this,
                recyclerView,
                currentListView,
                this::popupPresent,
                layoutInflater,
                this::currentTask,
                this::currentChecklist,
                currentUser
            )
            recyclerView.adapter = adapter

            addButton.visibility = View.INVISIBLE
            checkoffButton.visibility = View.INVISIBLE
            HistoryButton.visibility = View.INVISIBLE

            //adds the "New List..." button to list of list nav drawer
            leftsubMenu.add(0, Menu.FIRST + 1, Menu.FIRST, getString(R.string.ADD_LIST_TEXT))
                .setIcon(R.drawable.ic_add_box_black_24dp)
        }
        //if there's a local file, populate our list from that
        else if (listFileExists()) {
            spinner.visibility = View.VISIBLE
            currentChecklist.tasks = getListFromFile()
            //Need to do things with this information TODO
            //Currently this function recognizes if the completed
            //time is 2 days old, but the compared string is never set
            //and nothing is done
            //add each task in currentChecklist to the page
            for (Task in currentChecklist.tasks) {
                if (Task.compdatetime != null && Task.isRecurring != false) {
                    val now = LocalDate.now()
                    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                    val dt = formatter.parseDateTime(Task.compdatetime)
                    val dead = dt.plusDays(2)
                    if (!now.isEqual(dead.toLocalDate())) {
                        addTaskFromList(Task)
                    }
                } else
                    addTaskFromList(Task)
            }
            //sets the recyclerview adapter. This handles all recyclerview events, see "ChecklistAdapter for details.
            adapter = ChecklistAdapter(
                this,
                recyclerView,
                currentListView,
                this::popupPresent,
                layoutInflater,
                this::currentTask,
                this::currentChecklist,
                currentUser
            )
            recyclerView.adapter = adapter

            addButton.visibility = View.VISIBLE
            checkoffButton.visibility = View.VISIBLE
            HistoryButton.visibility = View.VISIBLE

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

                //mess of database calls
                currentChecklist.tasks = db.GetTasks(currentChecklist.listID!!)
                currentChecklist.users = db.GetUsers(currentChecklist.listID!!)
                currentChecklist.changes = db.GetChanges(currentChecklist.listID!!)
                currentListofLists.lists = db.GetListofLists(currentUser.Username)

                deleteListDataFile()
                deleteListsDataFile()
                createListFile(currentChecklist)
                createListsFile(currentListofLists)

                this@BaseChecklist.runOnUiThread {
                    //clears the user nav drawer
                    rightsubMenu.clear()
                    //fills the user nav drawer with info from database
                    for ((i, up) in currentChecklist.users.withIndex()) {
                        rightsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, up.Username)
                    }
                    //clears the list of list nav drawer
                    leftsubMenu.clear()
                    var ii = 0
                    //fills list of list nav drawer with info from server
                    for ((i, lol) in currentListofLists.lists.withIndex()) {
                        leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                        ii = i
                    }
                    //adds the "New List..." button to list of list nav drawer
                    leftsubMenu.add(0, Menu.FIRST + ii + 1, Menu.FIRST, getString(R.string.ADD_LIST_TEXT))
                        .setIcon(R.drawable.ic_add_box_black_24dp)
                    if (currentListofLists.lists.size != 0) {
                        //adds the "Edit List Name..." button to list of list nav drawer
                        leftsubMenu.add(0, Menu.FIRST + ii + 2, Menu.FIRST, getString(R.string.EDIT_LIST_TEXT))
                            .setIcon(R.drawable.ic_edit_black_24dp)
                        //adds the "Delete List..." button to list of list nav drawer
                        leftsubMenu.add(0, Menu.FIRST + ii + 3, Menu.FIRST, getString(R.string.DELETE_LIST_TEXT))
                            .setIcon(R.drawable.ic_delete_black_24dp)
                    }
                    currentListView.removeAll(currentListView)
                    //add each task in currentChecklist to the page
                    for (Task in currentChecklist.tasks) {
                        if (Task.compdatetime != null && Task.isRecurring == false) {
                            val now = LocalDateTime.now()
                            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                            val dt = formatter.parseDateTime(Task.compdatetime)
                            val dead = dt.plusDays(2)
                            if (now.isBefore(dead.toLocalDateTime())) {
                                addTaskFromList(Task)
                            }
                            else
                                db.DeleteTask(Task)
                        } else
                            addTaskFromList(Task)
                    }
                    //changes the dataset in the adapter to the one from the database
                    adapter.setDataset(currentListView)

                    addButton.visibility = View.VISIBLE
                    checkoffButton.visibility = View.VISIBLE
                    HistoryButton.visibility = View.VISIBLE

                    spinner.visibility = View.INVISIBLE
                }
                //remakes the local files
                deleteListDataFile()
                createListFile(currentChecklist)

                /*have a popup or something telling the user that the list has been updated*/
                turnOnButtons()

            }
            //}
        }

        //if no local file exists, populate our list from the database
        else {
            spinner.visibility = View.VISIBLE
            //sets the recyclerview adapter. This handles all recyclerview events, see "ChecklistAdapter for details.
            adapter = ChecklistAdapter(
                this,
                recyclerView,
                currentListView,
                this::popupPresent,
                layoutInflater,
                this::currentTask,
                this::currentChecklist,
                currentUser
            )
            //new thread
            GlobalScope.launch {
                //database calls
                currentChecklist.tasks = db.GetTasks(intent.getIntExtra("ChecklistID", 0))
                currentChecklist.users = db.GetUsers(intent.getIntExtra("ChecklistID", 0))
                currentChecklist.changes = db.GetChanges(intent.getIntExtra("ChecklistID", 0))
                currentListofLists.lists = db.GetListofLists(currentUser.Username)
                currentChecklist.listID = intent.getIntExtra("ChecklistID", 0)

                //do things in the GUI thread
                this@BaseChecklist.runOnUiThread {
                    //Same issue as "Need to do things with this information" TODO
                    //add each task in currentChecklist to the page
                    for (Task in currentChecklist.tasks) {
                        if (Task.compdatetime != null && Task.isRecurring == false) {
                            val now = LocalDateTime.now()
                            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                            val dt = formatter.parseDateTime(Task.compdatetime)
                            val dead = dt.plusDays(2)
                            if (now.isBefore(dead.toLocalDateTime())) {
                                addTaskFromList(Task)
                            }
                            else
                                db.DeleteTask(Task)
                        } else
                            addTaskFromList(Task)
                    }
                    //clears the user nav drawer
                    rightsubMenu.clear()
                    //fills the user nav drawer with info from database
                    for ((i, up) in currentChecklist.users.withIndex()) {
                        rightsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, up.Username)
                    }
                    //clears the list of list nav drawer
                    leftsubMenu.clear()
                    var ii = 0
                    //fills list of list nav drawer with info from server
                    for ((i, lol) in currentListofLists.lists.withIndex()) {
                        leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                        ii = i
                    }
                    //adds the "New List..." button to list of list nav drawer
                    leftsubMenu.add(0, Menu.FIRST + ii + 1, Menu.FIRST, getString(R.string.ADD_LIST_TEXT))
                        .setIcon(R.drawable.ic_add_box_black_24dp)
                    if (currentListofLists.lists.size != 0) {
                        //adds the "Edit List Name..." button to list of list nav drawer
                        leftsubMenu.add(0, Menu.FIRST + ii + 2, Menu.FIRST, getString(R.string.EDIT_LIST_TEXT))
                            .setIcon(R.drawable.ic_edit_black_24dp)
                        //adds the "Delete List..." button to list of list nav drawer
                        leftsubMenu.add(0, Menu.FIRST + ii + 3, Menu.FIRST, getString(R.string.DELETE_LIST_TEXT))
                            .setIcon(R.drawable.ic_delete_black_24dp)
                    }

                    //sets the recyclerview adapter. This handles all recyclerview events, see "ChecklistAdapter for details.
                    recyclerView.adapter = adapter

                    addButton.visibility = View.VISIBLE
                    checkoffButton.visibility = View.VISIBLE
                    HistoryButton.visibility = View.VISIBLE

                    //create a local file with the data
                    GlobalScope.launch {
                        createListFile(currentChecklist)
                    }
                    spinner.visibility = View.INVISIBLE
                }
            }

        }

        //populates the submenu with the usernames of everyone on the list (stored in mUserList)
        //Menu.FIRST + i gives each a unique ID, used later in the program.
        for ((i, up) in currentChecklist.users.withIndex()) {
            rightsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, up.Username)
        }

        //gets called whenever any item is selected in the user nav menu
        rightnavigationView.setNavigationItemSelectedListener { menuItem ->
            //handles all items in nav drawer that are created at compile time
            if (!onOptionsItemSelected(menuItem)) {
                //handles all items in nav drawer that are created at run time
                val id = menuItem.itemId - Menu.FIRST
                //when a user taps a button in the user list this makes sure that the button is actually in the list
                //and then jumps to the user page with the name of the person pressed
                if (id < currentChecklist.users.size && id >= 0) {
                    val tempIntent = Intent(this, UserLogin::class.java).apply {
                        putExtra("uname", currentChecklist.users[id].Username)
                        putExtra("fname", currentChecklist.users[id].FName)
                        putExtra("lname", currentChecklist.users[id].LName)
                        putExtra("UserID", currentChecklist.users[id].UserID!!)
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

            //makes sure the database is accessible
            if (hasInternetConnection()) {
                //handles all items in nav drawer that are created at compile time

                //when the item in a nav drawer is created it adds an incrementing number to Menu.FIRST. this assures a
                //diffrent number for every item that need it.
                val id = menuItem.itemId - Menu.FIRST
                //checks if the user is trying to delete a list
                if (!deleteFlag && !editFlag) {
                    //runs a function and doesn't run the next few line if anything was done in that function
                    if (!onOptionsItemSelected(menuItem)) {
                        //checks to make sure that the item pressed is a list and not add or delete
                        //if this runs then the user tapped a list and wants to switch to that list
                        if (id < currentListofLists.lists.size && id >= 0) {
                            //makes the loading spinner visible
                            spinner.visibility = View.VISIBLE
                            //closes the nav drawer
                            userLayout.closeDrawers()
                            //makes a new thread
                            GlobalScope.launch {
                                //disable certain actions while data is being loaded from database
                                turnOnButtons()
                                turnOffButtons()

                                //mess of database calls
                                currentChecklist.listID = currentListofLists.lists[id].listID
                                currentChecklist.tasks = db.GetTasks(currentChecklist.listID!!)
                                currentChecklist.users = db.GetUsers(currentChecklist.listID!!)
                                currentChecklist.changes = db.GetChanges(currentChecklist.listID!!)
                                currentChecklist.i_name = currentListofLists.lists[id].i_name
                                currentListofLists.lists = db.GetListofLists(currentUser.Username)

                                //runs on the GUI thread
                                this@BaseChecklist.runOnUiThread {
                                    //replaces the tasks and title on screen
                                    title = currentChecklist.i_name

                                    currentListView.removeAll(currentListView)
                                    //add each task in currentChecklist to the page
                                    for (Task in currentChecklist.tasks) {
                                        if (Task.compdatetime != null && Task.isRecurring != false) {
                                            val now = LocalDate.now()
                                            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                                            val dt = formatter.parseDateTime(Task.compdatetime)
                                            val dead = dt.plusDays(2)
                                            if (!now.isEqual(dead.toLocalDate())) {
                                                addTaskFromList(Task)
                                            }
                                        } else
                                            addTaskFromList(Task)
                                    }
                                    //changes the data set in the adapter, changing whats in recyclerview
                                    adapter.setDataset(currentListView)
                                    //runs the cascading list animation
                                    runLayoutAnimation()

                                    //replaces the user nav drawer items with the users in this list
                                    rightsubMenu.clear()
                                    for ((i, up) in currentChecklist.users.withIndex()) {
                                        rightsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, up.Username)
                                    }

                                    addButton.visibility = View.VISIBLE
                                    checkoffButton.visibility = View.VISIBLE
                                    HistoryButton.visibility = View.VISIBLE
                                    spinner.visibility = View.INVISIBLE
                                    // close drawer when item is tapped
                                }
                                //turns the buttons back on
                                turnOnButtons()
                            }
                            //checks if the selected item is the three items right after the list of list
                        } else if (id < currentListofLists.lists.size + 3 && id >= currentListofLists.lists.size) {
                            //checks if the selected item is the one right after the last list
                            if (id == currentListofLists.lists.size || (currentListofLists.lists.size == 0 && id == 1)) {
                                //if this code runs then the user wants to make a new list
                                if (!popupPresent) {
                                    //the following code makes a pop up with an edit field, add button, and close button
                                    val popupWindow = PopupWindow(this)

                                    val popupView = layoutInflater.inflate(R.layout.popup_layout, null)
                                    popupView.PopupEditText.hint = "Enter List Name"

                                    popupWindow.contentView = popupView

                                    val acceptButton = popupView.PopupMainView.AcceptButton

                                    //Creates and adds the on click action to the add button
                                    acceptButton.setOnClickListener {
                                        val popup_edittext = popupView.PopupMainView.PopupEditText
                                        val list_name = popup_edittext.text.toString()

                                        if (list_name.length >= 1) {
                                            spinner.visibility = View.VISIBLE
                                            GlobalScope.launch {
                                                //Create the new list and post it to the database
                                                val length = currentListofLists.lists.size
                                                currentListofLists.createList(
                                                    list_name, currentUser
                                                )
                                                while (currentListofLists.lists.size == length);
                                                this@BaseChecklist.runOnUiThread {
                                                    leftsubMenu.clear()
                                                    var ii = 0
                                                    for ((i, lol) in currentListofLists.lists.withIndex()) {
                                                        leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                                                        ii = i
                                                    }
                                                    leftsubMenu.add(
                                                        0, Menu.FIRST + ii + 1, Menu.FIRST,
                                                        getString(R.string.ADD_LIST_TEXT)
                                                    ).setIcon(R.drawable.ic_add_box_black_24dp)
                                                    if (currentListofLists.lists.size != 0) {
                                                        //adds the "Edit List Name..." button to list of list nav drawer
                                                        leftsubMenu.add(0, Menu.FIRST + ii + 2, Menu.FIRST, getString(R.string.EDIT_LIST_TEXT))
                                                            .setIcon(R.drawable.ic_edit_black_24dp)
                                                        leftsubMenu.add(
                                                            0,
                                                            Menu.FIRST + ii + 3,
                                                            Menu.FIRST,
                                                            getString(R.string.DELETE_LIST_TEXT)
                                                        ).setIcon(R.drawable.ic_delete_black_24dp)
                                                    }
                                                    spinner.visibility = View.INVISIBLE
                                                }
                                            }

                                            popupWindow.dismiss()

                                            //create a new coroutine that will
                                            //update the local LIST file to be current
                                            GlobalScope.launch {
                                                deleteListsDataFile()
                                                createListsFile(currentListofLists)
                                            }

                                            popupWindow.setOnDismissListener {
                                                PopupWindow.OnDismissListener {
                                                    popupPresent = false
                                                }
                                            }
                                        }
                                    }

                                    val cancelButton = popupView.PopupMainView.CancelButton

                                    cancelButton.setOnClickListener {

                                        popupWindow.dismiss()

                                    }

                                    popupWindow.setOnDismissListener {
                                        val popupEdittext = popupView.PopupMainView.PopupEditText

                                        popupEdittext.text.clear()

                                        popupPresent = false
                                    }

                                    popupWindow.isFocusable = true

                                    popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)

                                    popupPresent = true

                                }
                            } else if (id == currentListofLists.lists.size + 1) {
                                //if this code runs then the user wants to edit the name of a list
                                leftsubMenu.clear()
                                //replaces all the list of list items with ones with an x next to them so the user knows
                                // they can delete them when they tap an item. the add and delete buttons disapear.
                                for ((i, lol) in currentListofLists.lists.withIndex()) {
                                    leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                                        .setIcon(R.drawable.ic_edit_black_24dp)
                                }
                                editFlag = true
                            } else {
                                //if this code runs then the user wants to delete a list
                                leftsubMenu.clear()
                                //replaces all the list of list items with ones with an x next to them so the user knows
                                // they can delete them when they tap an item. the add and delete buttons disapear.
                                for ((i, lol) in currentListofLists.lists.withIndex()) {
                                    leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                                        .setIcon(R.drawable.ic_cancel_black_24dp)
                                }
                                deleteFlag = true
                            }
                        }

                    }
                } else if (!editFlag){
                    //if the delete flag is set run this
                    //if this runs the user wants to delete a list
                    if (id < currentListofLists.lists.size && id >= 0) {
                        deleteListDataFile(currentListofLists.lists[id].i_name)

                        //remove the task from the list, and delete it from the database
                        currentListofLists.deleteList(id, User(1))

                        //update the list of lists local file to be current
                        if (currentListofLists.lists.size > 0) {
                            GlobalScope.launch {
                                deleteListsDataFile()
                                createListsFile(currentListofLists)
                            }
                        }
                        else
                        {
                            deleteListsDataFile()
                        }

                        leftsubMenu.clear()
                        var ii = 0
                        for ((i, lol) in currentListofLists.lists.withIndex()) {
                            leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                            ii = i
                        }
                        leftsubMenu.add(
                            0, Menu.FIRST + ii + 1, Menu.FIRST,
                            getString(R.string.ADD_LIST_TEXT)
                        ).setIcon(R.drawable.ic_add_box_black_24dp)
                        if (currentListofLists.lists.size != 0) {
                            //adds the "Edit List Name..." button to list of list nav drawer
                            leftsubMenu.add(0, Menu.FIRST + ii + 2, Menu.FIRST, getString(R.string.EDIT_LIST_TEXT))
                                .setIcon(R.drawable.ic_edit_black_24dp)
                            leftsubMenu.add(
                                0,
                                Menu.FIRST + ii + 3,
                                Menu.FIRST,
                                getString(R.string.DELETE_LIST_TEXT)
                            ).setIcon(R.drawable.ic_delete_black_24dp)
                        }
                        else
                        {
                            //if this runs then the user deleted the list they were in
                            //removes the title and tasks
                            title = ""

                            currentListView.removeAll(currentListView)
                            //displays a message
                            var message = ChecklistViewModel("This list is empty. Select or create a new one.")
                            message.isMessage = true
                            currentListView.add(message)
                            //changes the data set in the adapter, changing whats in recyclerview
                            adapter.setDataset(currentListView)

                            addButton.visibility = View.INVISIBLE
                            checkoffButton.visibility = View.INVISIBLE
                            HistoryButton.visibility = View.INVISIBLE

                            //replaces the user nav drawer items with the users in this list
                            rightsubMenu.clear()

                            userLayout.closeDrawers()
                        }
                        spinner.visibility = View.INVISIBLE

                        deleteFlag = false
                    }
                }
                else {
                    //if the edit flag is set run this
                    //if this runs the user wants to edit the name of a list
                    if (id < currentListofLists.lists.size && id >= 0) {
                        //If there is not a popup already present
                        if (!popupPresent) {

                            val popupWindow = PopupWindow(this)
                            //Create a view that is of the popup_layout in resources
                            val popupView = layoutInflater.inflate(R.layout.popup_layout, null)
                            //Sets the content of the popup to the popup_layout
                            popupWindow.contentView = popupView
                            //set the hint to Edit List Name...
                            //Retrieves the acceptButton from the popup
                            val acceptButton = popupView.PopupMainView.AcceptButton

                            //Creates and adds the on click action to the add button
                            acceptButton.setOnClickListener {
                                val popup_edittext = popupView.PopupMainView.PopupEditText

                                //Retrieves the name of the task if the name is long enough
                                if (popup_edittext.text.toString().isNotEmpty() && popup_edittext.text.toString().length < 40) {
                                    if (hasInternetConnection()) {
                                        if (currentChecklist.i_name == currentListofLists.lists[id].i_name) {
                                            title = popup_edittext.text.toString()
                                            currentChecklist.name = popup_edittext.text.toString()
                                        }
                                        currentListofLists.changeListName(id, User(1), popup_edittext.text.toString())
                                        deleteListsDataFile()
                                        createListsFile(currentListofLists)


                                        //update the list of lists local file to be current
                                        GlobalScope.launch {
                                            deleteListsDataFile()
                                            createListsFile(currentListofLists)
                                        }

                                        leftsubMenu.clear()
                                        var ii = 0
                                        for ((i, lol) in currentListofLists.lists.withIndex()) {
                                            leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                                            ii = i
                                        }
                                        leftsubMenu.add(
                                            0, Menu.FIRST + ii + 1, Menu.FIRST,
                                            getString(R.string.ADD_LIST_TEXT)
                                        ).setIcon(R.drawable.ic_add_box_black_24dp)
                                        //adds the "Edit List Name..." button to list of list nav drawer
                                        leftsubMenu.add(0, Menu.FIRST + ii + 2, Menu.FIRST, getString(R.string.EDIT_LIST_TEXT))
                                            .setIcon(R.drawable.ic_edit_black_24dp)
                                        leftsubMenu.add(
                                            0,
                                            Menu.FIRST + ii + 3,
                                            Menu.FIRST,
                                            getString(R.string.DELETE_LIST_TEXT)
                                        ).setIcon(R.drawable.ic_delete_black_24dp)

                                        spinner.visibility = View.INVISIBLE

                                        editFlag = false
                                    }
                                    else
                                    {
                                        var alertDialog: AlertDialog = AlertDialog.Builder(this).create()

                                        alertDialog.setTitle("Info");
                                        alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again")
                                        alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                                        alertDialog.show()
                                    }
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

                            cancelButton.setOnClickListener {

                                popupWindow.dismiss()

                            }
                            //Have the popup clean up items when dismissed
                            popupWindow.setOnDismissListener {
                                val popupEdittext = popupView.PopupMainView.PopupEditText

                                popupEdittext.text.clear()

                                popupPresent = false
                            }

                            popupWindow.isFocusable = true

                            popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)

                            popupView.findViewById<EditText>(R.id.PopupEditText).hint = getString(R.string.EDIT_LIST_TEXT)

                            popupPresent = true

                        }
                    }
                }
            }
            true
        }

        //Creates the click listener for the add button
        val addListener = View.OnClickListener {

            //If there is not a popup already present
            if (!popupPresent) {

                val popupWindow = PopupWindow(this)
                //Create a view that is of the popup_layout in resources
                val popupView = layoutInflater.inflate(R.layout.popup_layout, null)
                //Sets the content of the popup to the popup_layout
                popupWindow.contentView = popupView
                //Retrieves the acceptButton from the popup
                val acceptButton = popupView.PopupMainView.AcceptButton

                //Creates and adds the on click action to the add button
                acceptButton.setOnClickListener {
                    val popup_edittext = popupView.PopupMainView.PopupEditText

                    //Retrieves the name of the task if the name is long enough
                    if (popup_edittext.text.toString().isNotEmpty() && popup_edittext.text.toString().length < 40) {
                        if (hasInternetConnection()) {
                            createNewTask(popup_edittext.text.toString(), false, 0/*needs to be something later*/)
                        }
                        else
                        {
                            var alertDialog: AlertDialog = AlertDialog.Builder(this).create()

                            alertDialog.setTitle("Info");
                            alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again")
                            alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                            alertDialog.show()
                        }
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

                cancelButton.setOnClickListener {

                    popupWindow.dismiss()

                }
                //Have the popup clean up items when dismissed
                popupWindow.setOnDismissListener {
                    val popupEdittext = popupView.PopupMainView.PopupEditText

                    popupEdittext.text.clear()

                    popupPresent = false
                }

                popupWindow.isFocusable = true

                popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)

                popupPresent = true

            }
        }

        addButton.setOnClickListener(addListener)
        //Create the click listener for the checkoff button
        val checkoffListener = View.OnClickListener {
            if (hasInternetConnection()) {
                var taskCount = currentListView.size - 1
                //Checks all current gui elements to see if they are checked
                while (taskCount >= 0) {
                    val currentChild = currentListView[taskCount]

                    if (currentChild.isChecked) {
                        if (!currentChild.isComplete) {
                            if (currentChild.isRecurring) {
                                /*createNewTask(
                                    currentChild.getTaskText(),
                                    true,
                                    0/*needs to be something later*/
                                )*/
                                //currentChecklist.createTask(currentChild.getTaskText(), "enable Later", User(1))
                            }

                            currentChild.completeTask()

                            //TaskLayout.removeView(TaskLayout.getChildAt(taskCount))

                            currentChecklist.completeTask(taskCount, currentUser)
                        }
                    }

                    taskCount--
                }
                adapter.datasetChange()
            }
            //this else clause happens when they have no internet connection
            else {
                var alertDialog: AlertDialog = AlertDialog.Builder(this).create()

                alertDialog.setTitle("Info");
                alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again")
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                alertDialog.show()
            }
        }

        checkoffButton.setOnClickListener(checkoffListener)

        //Set history button's click listener

        val historyListener = View.OnClickListener {

            if (!popupPresent) {

                val mainViewHistory = findViewById<RecyclerView>(R.id.checklist_recyclerview)

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
                if (currentChecklist.changes.isEmpty()) {
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
                } else {
                    historyIterator.forEach {

                        val checklistChangeTextView = TextView(this)

                        var toAddString = "Default"

                        when (it.changeType) {

                            kAction.CREATE_TASK -> toAddString = "--- Task Added: " + it.taskName +
                                    "\n\tAdded By: " + it.changedBy + "\n"
                            kAction.DELETE_TASK -> toAddString = "--- Task Deleted: " + it.taskName +
                                    "\n\tDeleted By: " + it.changedBy + "\n"
                            kAction.COMPLETE_TASK -> toAddString = "--- Task Completed: " + it.taskName +
                                    "\n\tCompleted By: " + it.changedBy + "\n"
                            kAction.CHANGE_TASK_NAME -> toAddString = "--- Task Edited: " + it.taskName +
                                    "\n\tChanged To: " + it.changedTo + "\n\tEdited By: " + it.changedBy + "\n"
                            kAction.CHANGE_TASK_DEADLINE -> toAddString = "--- Deadline Changed: " + it.taskName +
                                    "\n\tChanged To: " + it.changedTo + "\n\tEdited By: " + it.changedBy + "\n"
                            kAction.REMOVE_TASK_DEADLINE -> toAddString + "--- Deadline Removed: " + it.taskName +
                                    "\n\tRemoved By: " + it.changedBy + "\n"
                            kAction.ADD_USER -> toAddString = "--- User Added: " + it.changedTo +
                                    "\n\tAdded By: " + it.changedBy + "\n"
                            kAction.CHANGE_TASK_RECURRING -> toAddString = "--- Recursion Changed: " + it.taskName +
                                    "\n\tChanged To: " + it.changedTo + "\n\tEdited By: " + it.changedBy + "\n"
                            kAction.TASK_RECURRED -> "\n\tChanged To: " + it.changedTo
                            kAction.CHANGE_RECURRING_TIME -> toAddString = "--- Recursion Changed: " + it.taskName +
                                    "\n\tChanged To: " + it.changedTo + "\n\tEdited By: " + it.changedBy + "\n"
                            kAction.CHANGE_RECURRING_DAYS -> toAddString = "--- Recursion Changed: " + it.taskName +
                                    "\n\tChanged To: " + it.changedTo + "\n\tEdited By: " + it.changedBy + "\n"
                        }

                        if (toAddString != "Default") {
                            checklistChangeTextView.text = toAddString

                            checklistChangeTextView.setTextColor(Color.WHITE)

                            checklistChangeTextView.textSize = 20f
                            checklistChangeTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            checklistChangeTextView.gravity = Gravity.LEFT

                            historyLayout.addView(checklistChangeTextView)
                        }
                    }
                }

                popupWindowHistory.showAtLocation(mainViewHistory, Gravity.CENTER, 0, 0)
            }
        }

        historyButton.setOnClickListener(historyListener)
    }

    fun createNewTask(TaskText: String, IsReaccuring: Boolean, taskID: Int?) {
        var new_task_box = ChecklistViewModel(TaskText)

        if(IsReaccuring)
            new_task_box.setRecurringIfNotComplete(IsReaccuring)

        val size = currentChecklist.tasks.size

        //Adds the task to the checklist
        currentChecklist.createTask(TaskText, null, currentUser, null, currentChecklist.listID!!)

        new_task_box.taskID = currentChecklist.tasks[size].TaskID as Int

        //rebuild the local file with the updated checklist
        GlobalScope.launch {
            deleteListDataFile()
            createListFile(currentChecklist)
        }

        currentListView.add(0, new_task_box)
        adapter.setDataset(currentListView)
    }

    fun addTask(task: Task) {
        var new_task_box = ChecklistViewModel(task.name)

        new_task_box.taskID = task.TaskID as Int

        var found = false

        if(task.isRecurring == true)
        {
            if(task.compdatetime != null) {

                val recurringDays = split(task.recurringDays, "-")

                val now = LocalDate.now().dayOfWeek().asShortText

                val nowOther = LocalDate.now()
                val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                val dt = formatter.parseDateTime(task.compdatetime)

                for (i in 0 until recurringDays.size - 1) {
                    if (recurringDays[i] == now
                        && nowOther.isAfter(dt.toLocalDate())) {
                        found = true
                    }
                }
            }
            else {
                new_task_box.setRecurringIfNotComplete(true)
            }
        }
        if (task.compdatetime != null)
            new_task_box.completeTask()

        //Adds the task to the checklist
        currentChecklist.tasks.add(task)

        currentListView.add(new_task_box)

        if(found) {
            var taskCount = 0
            var taskFound = false
            //Checks all current gui elements to see if they are checked
            while (taskCount < currentChecklist.tasks.count() && !taskFound) {
                if(currentChecklist.tasks[taskCount].TaskID == task.TaskID)
                {
                    taskFound = true
                }
                else
                {
                    taskCount++
                }
            }

            currentChecklist.uncompleteTask(taskCount, currentUser)
        }
    }

    fun addTaskFromList(task: Task) {
        var new_task_box = ChecklistViewModel(task.name)

        var found = false

        new_task_box.taskID = task.TaskID as Int

        if(task.isRecurring == true)
        {
            if(task.compdatetime != null) {

                val recurringDays = split(task.recurringDays, "-")

                val now = LocalDate.now().dayOfWeek().asShortText

                val nowOther = LocalDate.now()
                val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                val dt = formatter.parseDateTime(task.compdatetime)

                for (i in 0 until recurringDays.size - 1) {
                    if (recurringDays[i] == now
                        && nowOther.isAfter(dt.toLocalDate())) {
                        found = true
                    }
                }
            }
            else {
                new_task_box.setRecurringIfNotComplete(true)
            }
        }

        if (task.compdatetime != null)
            new_task_box.completeTask()

        currentListView.add(new_task_box)

        if(found) {
            var taskCount = 0
            var taskFound = false
            //Checks all current gui elements to see if they are checked
            while (taskCount < currentChecklist.tasks.count() && !taskFound) {
                if(currentChecklist.tasks[taskCount].TaskID == task.TaskID)
                {
                    taskFound = true
                }
                else
                {
                    taskCount++
                }
            }

            currentChecklist.uncompleteTask(taskCount, currentUser)
        }
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
                if (deleteFlag || editFlag)
                {
                    leftsubMenu.clear()
                    var ii = 0
                    for ((i, lol) in currentListofLists.lists.withIndex()) {
                        leftsubMenu.add(0, Menu.FIRST + i, Menu.FIRST, lol.i_name)
                        ii = i
                    }
                    leftsubMenu.add(
                        0, Menu.FIRST + ii + 1, Menu.FIRST,
                        getString(R.string.ADD_LIST_TEXT)
                    ).setIcon(R.drawable.ic_add_box_black_24dp)
                    if (currentListofLists.lists.size != 0) {
                        //adds the "Edit List Name..." button to list of list nav drawer
                        leftsubMenu.add(0, Menu.FIRST + ii + 2, Menu.FIRST, getString(R.string.EDIT_LIST_TEXT))
                            .setIcon(R.drawable.ic_edit_black_24dp)
                        leftsubMenu.add(
                            0,
                            Menu.FIRST + ii + 3,
                            Menu.FIRST,
                            getString(R.string.DELETE_LIST_TEXT)
                        ).setIcon(R.drawable.ic_delete_black_24dp)
                    }
                    deleteFlag = false
                    editFlag = false
                }
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
            R.id.oUser -> {
                val tempIntent = Intent(this, UserLogin::class.java).apply {
                    putExtra("uname", currentUser.Username)
                    putExtra("fname", currentUser.FName)
                    putExtra("lname", currentUser.LName)
                    putExtra("UserID", currentUser.UserID)
                }
                startActivity(tempIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (hasInternetConnection()) {
            // Inflate the options menu from XML
            val inflater = menuInflater
            val db = Database()
            inflater.inflate(R.menu.options_menu, menu)

            val searchItem = menu.findItem(R.id.search)
            if (searchItem != null) {
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        for (i in currentChecklist.users)
                        {
                            if (i.Username.toUpperCase() == (query as String).toUpperCase())
                            {
                                Toast.makeText(this@BaseChecklist, "User already on list.", Toast.LENGTH_SHORT).show()
                                searchItem.collapseActionView()
                                val activity: Activity = this@BaseChecklist
                                val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                                //Find the currently focused view, so we can grab the correct window token from it.
                                var view = activity.currentFocus
                                //If no view currently has focus, create a new one, just so we can grab a window token from it
                                if (view == null) {
                                    view = View(activity)
                                }
                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                                return true
                            }
                        }
                        val u = db.GetUser(query as String)
                        if (u.UserID != -1) {
                            db.AddUserToList(u.UserID as Int, currentChecklist.listID as Int)
                            rightsubMenu.add(0, Menu.FIRST + 7, Menu.FIRST, u.Username)
                        }
                        else
                        {
                            Toast.makeText(this@BaseChecklist, "User does not exist.", Toast.LENGTH_SHORT).show()
                        }
                        searchItem.collapseActionView()
                        val activity: Activity = this@BaseChecklist
                        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        //Find the currently focused view, so we can grab the correct window token from it.
                        var view = activity.currentFocus
                        //If no view currently has focus, create a new one, just so we can grab a window token from it
                        if (view == null) {
                            view = View(activity)
                        }
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return true
                    }

                })
            }

            return super.onCreateOptionsMenu(menu)
        }

        //this clause happens if they have no internet connection
        else {
            return false
        }
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

    private fun hasInternetConnection() : Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        return isConnected
    }

    fun runLayoutAnimation() {
        val context : Context = recyclerView.context
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        recyclerView.layoutAnimation = controller
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }

    override fun onBackPressed() {
    //We don't want the back button to do anything
    }
}