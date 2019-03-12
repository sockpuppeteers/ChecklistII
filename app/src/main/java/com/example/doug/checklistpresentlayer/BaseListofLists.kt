package com.example.doug.checklistpresentlayer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
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
class BaseListofLists : AppCompatActivity(){

    var UName = ""
    var FName = ""
    var LName = ""

    var inEdit = false
    var currentListofLists = ListofLists("Your CheckLists", "none")

    //Flag to see if any popups are present
    var popupPresent = false

    var currentTask: TaskBox? = null

    private lateinit var drawerLayout: DrawerLayout

    //Intialize things here
    init {

    }
//
//    /********************************************
//     *Purpose: Click Listener for the edit button
//     *
//     * DO NOT USE
//     ********************************************/
//    val edit_listener = View.OnClickListener {
//
//        var taskCount = TaskLayout.childCount - 1
//
//        while (taskCount >= 0)
//        {
//            val currentChild = TaskLayout.getChildAt(taskCount)
//
//            if(currentChild is TaskBox)
//            {
//                val taskSwitch = currentChild.getChildAt(1)
//
//                if(taskSwitch is Switch)
//                {
//                    if(taskSwitch.isChecked)
//                    {
//                        TaskLayout.removeView(TaskLayout.getChildAt(taskCount))
//                    }
//                }
//            }
//
//            taskCount--
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_listoflists)

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

        val addButton = findViewById<Button>(R.id.AddListButton)

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
        val addListener = View.OnClickListener {

            if(!popupPresent) {

                val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

                val popupWindow = PopupWindow(this)

                val popupView = layoutInflater.inflate(R.layout.popup_layout, null)
                popupView.PopupEditText.hint = "Enter List Name"

                popupWindow.contentView = popupView

                val acceptButton = popupView.PopupMainView.AcceptButton

                //Creates and adds the on click action to the add button
                acceptButton.setOnClickListener(
                    View.OnClickListener {


                        val popup_edittext = popupView.PopupMainView.PopupEditText

                        val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)

                        if (popup_edittext.text.toString().length >= 1) {
                            var new_list_box = ListBox(
                                this,
                                popup_edittext.text.toString()
                            )

                            currentListofLists.createList(popup_edittext.text.toString(), "enable Later", User(1))

                            popupWindow.dismiss()

                            popupWindow.setOnDismissListener { PopupWindow.OnDismissListener {
                                popupPresent = false
                            } }

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

                            val DeleteListener = View.OnClickListener {
                                for(i in TaskLayout.childCount downTo 0 step 1)
                                {
                                    val tempChild = TaskLayout.getChildAt(i)
                                    if(tempChild is TaskBox)
                                    {
                                        if(tempChild.getTaskText() == currentTask?.getTaskText())
                                        {
                                            TaskLayout.removeView(TaskLayout.getChildAt(i))
                                            currentListofLists.deleteList(i, User(1));
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
                                    popupPresent = false;
                                }
                            }

                            new_list_box.setOnClickListener(View.OnClickListener {
                                val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                                    putExtra("ListName", popup_edittext.text.toString())
                                    putExtra("UserName", UName)
                                }
                                startActivity(tempIntent)
                            })

                            taskLayout.addView(new_list_box)
                        }
                })

                val cancelButton = popupView.PopupMainView.CancelButton

                cancelButton.setOnClickListener(View.OnClickListener {

                    popupWindow.dismiss()

                })

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

    }

//    fun initNavigationDrawer() {
//
//        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
//        navigationView.setNavigationItemSelectedListener { menuItem ->
//            val id = menuItem.itemId
//
//            when (id) {
//                R.id.dProfile -> {
//                    val tempIntent = Intent(this, UserLogin::class.java).apply {
//                        putExtra("UserName", UName)
//                        putExtra("fname", FName)
//                        putExtra("lname", LName)
//                    }
//                    startActivity(tempIntent)
//                }
//                R.id.dSettings -> {
//                }
//                R.id.dLogOut -> {
//                }
//                else -> {
//                }
//            }//same logic
//            //same logic
//            true
//        }
//    }
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
