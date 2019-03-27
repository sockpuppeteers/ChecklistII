/***************
 * note: most of this code is Emmett's
 */
package com.example.doug.checklistpresentlayer

import android.content.Intent
import khttp.*
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.widget.*
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*


class BaseListofLists : AppCompatActivity(){

    var UName = ""
    var FName = ""
    var LName = ""
    var currentListofLists = ListofLists("Your CheckLists", "none")
    //Flag to see if any popups are present
    var popupPresent = false

    var currentTask: TaskBox? = null

    private lateinit var drawerLayout: DrawerLayout

    //Intialize things here
    init {


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_listoflists)
        currentListofLists.uID = intent.getIntExtra("UserID", 0)
        var db = Database()
        UName = intent.getStringExtra("uname")
        currentListofLists.lists = db.GetListofLists(UName)
        //Get layout of checklist names
        val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)
        var tempBox: ListBox

        for (ListClass in currentListofLists.lists) {
            //Fill box with checklist name
            tempBox = ListBox(
                this,
                ListClass.i_name
            )

            //Set The action to be executed when the list in clicked
            tempBox.setOnClickListener{
                val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                    putExtra("ChecklistID", ListClass.listID)
                    putExtra("uname", intent.getStringExtra("uname"))
                    putExtra("fname", intent.getStringExtra("fname"))
                    putExtra("lname", intent.getStringExtra("lname"))
                    putExtra("UserID",intent.getIntExtra("UserID", 0))
                }

                //Start the BaseChecklist Activity
                startActivity(tempIntent)
            }

            //Add box to page
            taskLayout.addView(tempBox)
        }
        UName = intent.getStringExtra("uname")
        FName = intent.getStringExtra("fname")
        LName = intent.getStringExtra("lname")

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            onOptionsItemSelected(menuItem)
            // close drawer when item is tapped
            drawerLayout.closeDrawers()
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true
        }

        val addButton = findViewById<Button>(R.id.AddListButton)

        val addListener = View.OnClickListener {

            if(!popupPresent) {

                val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

                val popupWindow = PopupWindow(this)

                val popupView = layoutInflater.inflate(R.layout.popup_layout, null)
                popupView.PopupEditText.hint = "Enter List Name"

                popupWindow.contentView = popupView

                val acceptButton = popupView.PopupMainView.AcceptButton

                //Creates and adds the on click action to the add button
                acceptButton.setOnClickListener{
                    val popup_edittext = popupView.PopupMainView.PopupEditText

                    val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)

                    if (popup_edittext.text.toString().length >= 1) {
                        var new_list_box = ListBox(
                            this,
                            popup_edittext.text.toString()
                        )

                        currentListofLists.createList(popup_edittext.text.toString(),
                            User(intent.getIntExtra("UserID", 0)))/*, intent.getStringExtra("uname"),
                                 intent.getStringExtra("fname"), intent.getStringExtra("lname")))*/
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
                                        currentListofLists.deleteList(i,
                                            UserPage(currentListofLists.uID, UName, FName, LName, "none"))
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

//                        new_list_box.setOnTouchListener(object : View.OnTouchListener {
//                            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                                when (event?.action) {
//                                    MotionEvent.ACTION_DOWN -> //Do Something
//                                }
//
//                                return v?.onTouchEvent(event) ?: true
//                            }
//                        })

                        new_list_box.isLongClickable = true
                        new_list_box.isClickable = true

                        new_list_box.setOnLongClickListener{

                            if(!popupPresent) {

                                popupPresent = true

                                popupFunctionWindow.isFocusable()

                                popupFunctionWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                                for(i in TaskLayout.childCount downTo 0 step 1)
                                {
                                    val tempChild = TaskLayout.getChildAt(i)
                                    if(tempChild is TaskBox)
                                    {
                                        if(tempChild == new_list_box) {
                                            currentTask = tempChild
                                        }
                                    }
                                }
                            }
                            return@setOnLongClickListener true
                        }
                        new_list_box.setOnClickListener{
                            val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                                putExtra("ListName", popup_edittext.text.toString())
                                putExtra("UserName", UName)
                                putExtra("ChecklistID", currentListofLists.lists.last().listID)
                                putExtra("uname", intent.getStringExtra("uname"))
                                putExtra("fname", intent.getStringExtra("fname"))
                                putExtra("lname", intent.getStringExtra("lname"))
                                putExtra("UserID",intent.getIntExtra("UserID", 0))
                            }
                            startActivity(tempIntent)
                        }

                        taskLayout.addView(new_list_box)
                    }
                }

                val cancelButton = popupView.PopupMainView.CancelButton

                cancelButton.setOnClickListener{

                    popupWindow.dismiss()

                }

                popupWindow.setOnDismissListener{
                    val popupEdittext = popupView.PopupMainView.PopupEditText

                    popupEdittext.text.clear()

                    popupPresent = false
                }

                popupWindow.isFocusable = true

                popupWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                popupPresent = true

            }
        }

        addButton.setOnClickListener(addListener)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.dProfile -> {
                val tempIntent = Intent(this, UserLogin::class.java).apply {
                    putExtra("uname", UName)
                    putExtra("fname", FName)
                    putExtra("lname", LName)
                }
                startActivity(tempIntent)
                true
            }
            R.id.dSettings -> {
                true
            }
            R.id.dLogOut -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
