/***************
 * note: most of this code is Emmett's
 */
package com.example.doug.checklistpresentlayer

import android.content.Intent
import android.content.pm.ActivityInfo
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
import kotlinx.android.synthetic.main.activity_base_listoflists.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_delete_layout.view.*
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

    private lateinit var drawerLayout: DrawerLayout

    //Initialize things here
    init { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_base_listoflists)
        currentListofLists.uID = intent.getIntExtra("UserID", 0)
        UName = intent.getStringExtra("uname")
        FName = intent.getStringExtra("fname")
        LName = intent.getStringExtra("lname")


        //Get layout of checklist names
        val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)
        var tempBox: ListBox
        val cont = this

        val spinner : ProgressBar = findViewById(R.id.lprogress_bar)
        //if the lists are stored locally in the db
        //then we can load it from there
        if (listsFileExists()){
            spinner.visibility = View.VISIBLE

            currentListofLists.lists = getListFromFile()

            //check the db to see if the user
            GlobalScope.launch {
                var db = Database()
                var list = db.GetListofLists(UName)

                if (!list.isEmpty())
                    currentListofLists.lists = list

                this@BaseListofLists.runOnUiThread {
                    if (!list.isEmpty()){
                        taskLayout.removeAllViews()
                        for (ListClass in currentListofLists.lists) {
                            //Fill box with checklist name
                            tempBox = ListBox(
                                cont,
                                ListClass.i_name
                            )

                            //Set The action to be executed when the list in clicked
                            tempBox.setOnClickListener{
                                val tempIntent = Intent(cont, BaseChecklist::class.java).apply {
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

                            //Add box to page
                            taskLayout.addView(tempBox)
                        }
                    }

                    spinner.visibility = View.INVISIBLE
                }
            }
        }
        //otherwise query the api for our data
        else{
            //get the user's lists from the database
            var db = Database()
            currentListofLists.lists = db.GetListofLists(UName)

            //put those lists in a local file
            createListsFile(currentListofLists)

            spinner.visibility = View.INVISIBLE
        }

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

        val DeleteListener = View.OnClickListener {
            if(!popupPresent) {
                val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

                val popupWindow = PopupWindow(this)

                val popupView = layoutInflater.inflate(R.layout.popup_delete_layout, null)

                popupWindow.contentView = popupView

                for (ListClass in currentListofLists.lists) {
                    var tempBox2 = ListBox(
                        this,
                        ListClass.i_name
                    )

                    //Set The action to be executed when the list is clicked
                    tempBox2.setOnClickListener{
                        for(i in popupView.DeleteLayout.childCount downTo 0 step 1)
                        {
                            val tempChild = popupView.DeleteLayout.getChildAt(i)
                            if (tempChild is ListBox)
                            {
                                if (tempChild == tempBox2)
                                {
                                    taskLayout.removeView(taskLayout.getChildAt(i))

                                    //delete the list's local file
                                    GlobalScope.launch {
                                        deleteListDataFile(currentListofLists.lists[i].i_name)
                                    }

                                    //remove the task from the list, and delete it from the database
                                    currentListofLists.deleteList(i, User(1))

                                    //update the list of lists local file to be current
                                    GlobalScope.launch {
                                        deleteListsDataFile()
                                        createListsFile(currentListofLists)
                                    }
                                }
                            }
                        }

                        popupPresent = false
                        popupWindow.dismiss()
                    }

                    //Add box to page
                    popupView.DeleteLayout.addView(tempBox2)
                }

                val closeButton = popupView.CloseButton

                closeButton.setOnClickListener{
                    popupWindow.dismiss()
                }

                popupWindow.setOnDismissListener{
                    popupPresent = false
                }

                popupWindow.isFocusable = true

                popupWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                popupPresent = true
            }
        }

        addButton.setOnClickListener(addListener)
        DeleteListButton.setOnClickListener(DeleteListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
//            R.id.dProfile -> {
//                val tempIntent = Intent(this, UserLogin::class.java).apply {
//                    putExtra("uname", UName)
//                    putExtra("fname", FName)
//                    putExtra("lname", LName)
//                }
//                startActivity(tempIntent)
//                true
//            }
//            R.id.dSettings -> {
//                true
//            }
//            R.id.dLogOut -> {
//                //delete local data files
//                deleteUserDataFile()
//                deleteListsDataFile()
//
//                //redirect to the login page
//                val tempIntent = Intent(this, MainActivity::class.java)
//                startActivity(tempIntent)
//                true
//            }
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
}
