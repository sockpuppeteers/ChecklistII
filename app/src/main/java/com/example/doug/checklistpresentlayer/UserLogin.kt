package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.example.doug.checklistpresentlayer.R.drawable.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream

//This is actually the nav drawer functionality from user profile page
class UserLogin : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        val UName = intent.getStringExtra("uname")
        val FName = intent.getStringExtra("fname")
        val LName = intent.getStringExtra("lname")
        FLName.text = FName + " " + LName
        UserName.text = "(" + UName + ")"

        Icon.setImageResource(icon1)

        val logout = findViewById<TextView>(R.id.Logout)
        logout.setOnClickListener{
            deleteUserDataFile()
            deleteListsDataFile()

            //redirect to the login page
            val tempIntent = Intent(this, MainActivity::class.java)
            startActivity(tempIntent)
        }

        if (userFileExists()){
            if (getUserFromFile().ViewUserName() != UName)
                logout.visibility = View.INVISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.onBackPressed()
        return true
    }

    fun deleteUserDataFile(){
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "USERDATA"
        val directory = context.filesDir

        //delete the USERDATA file
        File(directory, filename).delete()
    }

    fun deleteListsDataFile(){
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "LISTS"
        val directory = context.filesDir

        //delete the USERDATA file
        File(directory, filename).delete()
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
}