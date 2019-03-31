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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class BaseListofLists : AppCompatActivity(){

    var UName = ""
    var FName = ""
    var LName = ""
    var currentListofLists = ListofLists("Your CheckLists", "none")
    //Flag to see if any popups are present
    var popupPresent = false

    var currentTask: TaskBox? = null

    private lateinit var drawerLayout: DrawerLayout

    //Initialize things here
    init { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_listoflists)
        currentListofLists.uID = intent.getIntExtra("UserID", 0)
        UName = intent.getStringExtra("uname")
        FName = intent.getStringExtra("fname")
        LName = intent.getStringExtra("lname")

        //if the lists are stored locally in the db
        //then we can load it from there
        if (listsFileExists()){
            currentListofLists.lists = getListFromFile()
            println("from local file")
        }
        //otherwise query the api for our data
        else{
            //get the user's lists from the database
            var db = Database()
            currentListofLists.lists = db.GetListofLists(UName)

            //put those lists in a local file
            createListsFile(currentListofLists)
            println("from db")
        }

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
                    putExtra("ListName", ListClass.i_name)
                    putExtra("UserName", UName)
                    putExtra("ChecklistID", ListClass.listID)
                    putExtra("uname", UName)
                    putExtra("fname", FName)
                    putExtra("lname", LName)
                    putExtra("UserID",intent.getIntExtra("UserID", 0))
                }

                //Start the BaseChecklist Activity
                startActivity(tempIntent)
            }

            //this line may do nothing actually, will double check later
            tempBox.gravity = Gravity.LEFT

            //Add box to page
            taskLayout.addView(tempBox)
        }

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

                            GlobalScope.launch {
                                //Create the new list and post it to the database
                                currentListofLists.createList(popup_edittext.text.toString(),
                                    User(intent.getIntExtra("UserID", 0)))
                            }

                            popupWindow.dismiss()

                            //create a new coroutine that will
                            //update the local LIST file to be current
                            GlobalScope.launch {
                                deleteListsDataFile()
                                createListsFile(currentListofLists)
                            }

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
                                    popupPresent = false
                                }
                            }

                            new_list_box.setOnClickListener{
                                val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                                    putExtra("ListName", popup_edittext.text.toString())
                                    putExtra("UserName", UName)
                                    putExtra("ChecklistID", currentListofLists.lists.last().listID)
                                    putExtra("uname", UName)
                                    putExtra("fname", FName)
                                    putExtra("lname", LName)
                                    putExtra("UserID",intent.getIntExtra("UserID", 0))
                                }
                                startActivity(tempIntent)
                            }

                            //this line may do nothing actually, will double check later
                            new_list_box.gravity = Gravity.LEFT
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
    fun getListFromFile() : MutableList<ListClass> {
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

    fun deleteListsDataFile(){
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "LISTS"
        val directory = context.filesDir

        //delete the LISTS file
        File(directory, filename).delete()
    }
}
