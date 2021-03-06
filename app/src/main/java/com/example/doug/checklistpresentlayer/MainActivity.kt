package com.example.doug.checklistpresentlayer

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import net.danlew.android.joda.JodaTimeAndroid
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast


//This is actually the user login page functionality
class MainActivity : AppCompatActivity() {
    var lightsAnimation: AnimationDrawable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this)
        setContentView(R.layout.activity_login)
        val ctext = this
        val lights = findViewById<ImageView>(R.id.imageView)
        lightsAnimation = lights.drawable as AnimationDrawable

        var user: UserPage
        val spinner : ProgressBar = findViewById(R.id.progress_bar)

        //if the USERDATA file exists already, the user doesn't
        //have to go through the login process
        if(userFileExists() && hasInternetConnection()){
            user = getUserFromFile()

            //check if the user has any checklists
            if (listsFileExists()){
                var lists = getListFromFile()

                if (lists.isEmpty()){
                    var db = Database()
                    lists = db.GetListofLists(user.ViewUserName())
                }

                if (!lists.isEmpty()){
                    //Go to the checklist page of the first checklist in lists
                    val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                        putExtra("ListName", lists[0].i_name)
                        putExtra("UserName", user.ViewUserName())
                        putExtra("ChecklistID", lists[0].listID)
                        putExtra("uname", user.ViewUserName())
                        putExtra("fname", user.ViewFName())
                        putExtra("lname", user.ViewLName())
                        putExtra("UserID", user.ViewID())
                    }
                    startActivity(tempIntent)
                }

                else {
                    //Go to the checklist page of the my first list
                    val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                        putExtra("ListName", "NULLLIST")
                        putExtra("UserName", user.ViewUserName())
                        putExtra("ChecklistID", -1)
                        putExtra("uname", user.ViewUserName())
                        putExtra("fname", user.ViewFName())
                        putExtra("lname", user.ViewLName())
                        putExtra("UserID", user.ViewID()!!)
                    }
                    startActivity(tempIntent)
                }
            }

            //create a new checklist called "My First List"
            else {
                //Go to the checklist page of the my first list
                val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                    putExtra("ListName", "NULLLIST")
                    putExtra("UserName", user.ViewUserName())
                    putExtra("ChecklistID", -1)
                    putExtra("uname", user.ViewUserName())
                    putExtra("fname", user.ViewFName())
                    putExtra("lname", user.ViewLName())
                    putExtra("UserID", user.ViewID()!!)
                }
                startActivity(tempIntent)
            }
        }
        else
        {
            if (!hasInternetConnection()) {
                var alertDialog: AlertDialog = AlertDialog.Builder(this).create()

                alertDialog.setTitle("Info");
                alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again")
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                alertDialog.show()
            }
        }
        var an = findViewById<TextView>(R.id.AppName)
        an.visibility = View.INVISIBLE
        lightsAnimation!!.start()
        GlobalScope.launch {
            delay(1500)
            this@MainActivity.runOnUiThread {
                val `in` = AlphaAnimation(0.0f, 1.0f)
                `in`.duration = 3000
                an.startAnimation(`in`)
                an.visibility = View.VISIBLE
            }
        }

        login_button.setOnClickListener {
            deleteListsDataFile()
            if (hasInternetConnection()) {
                //sets the loading spinner to visible and to not fill as it goes, just to spin
                spinner.visibility = View.VISIBLE
                spinner.isIndeterminate = true
                //establish a database connection
                val db = Database()
                //try to login
                GlobalScope.launch {
                    user = db.LogIn(lUN.text.toString(), lPW.text.toString())

                    //runs this code on the gui thread so we can set errors, this code is fast so shouldn't effect runtime
                    this@MainActivity.runOnUiThread {
                        //if there was an error, display it
                        if (user.HasError()) {
                            if (user.ViewError() == "404")
                                Toast.makeText(this@MainActivity, "Wrong username or password.", Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(this@MainActivity, "Something went wrong.", Toast.LENGTH_SHORT).show()
                        }

                        //otherwise, log the user in and transfer to the list of lists page
                        else {
                            //save a local file with the user's data
                            //so that they won't have to login next time
                            saveLoginLocally(user)

                            var listofLists = db.GetListofLists(user.ViewUserName())

                            if (listofLists.isEmpty()){
                                //Go to the checklist page of the my first list
                                val tempIntent = Intent(ctext, BaseChecklist::class.java).apply {
                                    putExtra("ListName", "NULLLIST")
                                    putExtra("UserName", user.ViewUserName())
                                    putExtra("ChecklistID", -1)
                                    putExtra("uname", user.ViewUserName())
                                    putExtra("fname", user.ViewFName())
                                    putExtra("lname", user.ViewLName())
                                    putExtra("UserID", user.ViewID()!!)
                                }
                                startActivity(tempIntent)
                            }

                            else {
                                var lists = ListofLists(user.ViewUserName(), null, user.ViewID()!!)
                                lists.lists = listofLists

                                //update local file
                                deleteListsDataFile()
                                createListsFile(lists)

                                //Go to the checklist page of the first checklist in lists
                                val tempIntent = Intent(ctext, BaseChecklist::class.java).apply {
                                    putExtra("ListName", lists.lists[0].i_name)
                                    putExtra("UserName", user.ViewUserName())
                                    putExtra("ChecklistID", lists.lists[0].listID)
                                    putExtra("uname", user.ViewUserName())
                                    putExtra("fname", user.ViewFName())
                                    putExtra("lname", user.ViewLName())
                                    putExtra("UserID", user.ViewID()!!)
                                }
                                startActivity(tempIntent)
                            }
                        }

                        //turns on loading spinner
                        spinner.visibility = View.INVISIBLE
                    }
                }
            }

            //this happens if they have no internet connection
            else{
                var alertDialog : AlertDialog = AlertDialog.Builder(this).create()

                alertDialog.setTitle("Info")
                alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again")
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                alertDialog.show()
            }
        }

        register_text_button.setOnClickListener {
            if (hasInternetConnection()){
                deleteListsDataFile()
                val tempIntent = Intent(this, Registration::class.java)
                startActivity(tempIntent)
            }
            else{
                var alertDialog : AlertDialog = AlertDialog.Builder(this).create()

                alertDialog.setTitle("Info");
                alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again")
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                alertDialog.show()
            }
        }
    }

    fun saveLoginLocally(user: UserPage){
        //convert user to a JSON string
        val gson = Gson()
        val userJson = gson.toJson(user)

        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "USERDATA"
        val directory = context.filesDir

        //write the file USERDATA to local directory
        val file = File(directory, filename)
        FileOutputStream(file).use {
            it.write(userJson.toByteArray())
        }
    }

    fun userFileExists() : Boolean {
        return File(applicationContext.filesDir, "USERDATA").exists()
    }

    //we don't have to check if the file exists in this function
    //because we call userFileExists() before calling this
    //however, we might need some other error checking in here
    fun getUserFromFile() : UserPage {
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "USERDATA"
        val directory = context.filesDir

        //read from USERDATA and store it as a string
        val file = File(directory, filename)
        val fileData = FileInputStream(file).bufferedReader().use { it.readText() }

        //create a UserPage object based on the JSON from the file
        val gson = Gson()
        return gson.fromJson(fileData, UserPage::class.java)
    }

    private fun hasInternetConnection() : Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        return isConnected
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
    
    override fun onBackPressed() {

    }
}