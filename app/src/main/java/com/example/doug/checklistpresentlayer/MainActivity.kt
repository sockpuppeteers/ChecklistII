package com.example.doug.checklistpresentlayer

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

//This is actually the user login page functionality
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var user: UserPage

        //if the USERDATA file exists already, the user doesn't
        //have to go through the login process
        if(userFileExists()){
            user = getUserFromFile()

            //Go to the list of lists page
            val tempIntent = Intent(this, BaseListofLists::class.java).apply {
                putExtra("uname", user.ViewUserName())
                putExtra("fname", user.ViewFName())
                putExtra("lname", user.ViewLName())
                putExtra("UserID",user.ViewID())
            }

            startActivity(tempIntent)
        }

        login_button.setOnClickListener {
            //establish a database connection
            val db = Database()
            //try to login
            user = db.LogIn(lUN.text.toString(), lPW.text.toString())

            //if there was an error, display it
            if (user.HasError()) {
                if (user.ViewError() == "404")
                    ErrorText.text = "Incorrect username or password"
                else
                    ErrorText.text = "Something went wrong"
            }

            //otherwise, log the user in and transfer to the list of lists page
            else {
                //save a local file with the user's data
                //so that they won't have to login next time
                saveLoginLocally(user)

                //Go to the list of lists page
                val tempIntent = Intent(this, BaseListofLists::class.java).apply {
                        putExtra("uname", user.ViewUserName())
                        putExtra("fname", user.ViewFName())
                        putExtra("lname", user.ViewLName())
                        putExtra("UserID",user.ViewID())
                }
                startActivity(tempIntent)
            }
        }

        register_text_button.setOnClickListener {
            val tempIntent = Intent(this, Registration::class.java)
            startActivity(tempIntent)
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
}