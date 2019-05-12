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
import java.io.File

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
}