package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.content.pm.ActivityInfo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.regex.Pattern


class Registration : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_registration)
        var pwMatch = true
        //boolean that checks if the username is US ASCII (could be weird for multi language features)
        var userAscii = Charset.forName("US-ASCII").newEncoder().canEncode(rUserName.text.toString())
        //same as above for pw
        var pwAscii =  Charset.forName("US-ASCII").newEncoder().canEncode(rPW1.text.toString())
        register_button.setOnClickListener {
            //create a database object with the given username
            val db = Database()
            //checks to see if passwords match
            if (rPW1.text.toString() != rPW2.text.toString())
                pwMatch = false
            if (userAscii && pwAscii && pwMatch && pwStrong(rPW1.text.toString())) {
                //error will be empty if everything was successful
                var user: User = db.RegisterUser(rUserName.text.toString(),
                    rEmail.text.toString(), rFName.text.toString(), rLName.text.toString(),
                    rPW1.text.toString())
                if (user.Error != "" && user.Error != null)
                    rErrorText.text = user.Error

                //else will be called if there were no errors
                else {
                    var lists = ListofLists(user.Username, null, user.UserID!!)

                    //create a new checklist
                    lists.PostChecklist(ListClass(null, "My First List"))

                    //update local file
                    deleteListsDataFile()
                    createListsFile(lists)

                    //Go to the checklist page of the my first list
                    val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                        putExtra("ListName", lists.lists[0].i_name)
                        putExtra("UserName", user.Username)
                        putExtra("ChecklistID", lists.lists[0].listID)
                        putExtra("uname", user.Username)
                        putExtra("fname", user.FName)
                        putExtra("lname", user.LName)
                        putExtra("UserID", user.UserID!!)
                    }
                    startActivity(tempIntent)
                }
            }
        }
    }

    //passwords must be at least 8 characters long
    fun pwStrong(str : String) : Boolean {
        return str.length >= 8
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

    //deletes the file that contains list of lists data
    fun deleteListsDataFile(){
        //context will give us access to our local files directory
        var context = applicationContext

        val filename = "LISTS"
        val directory = context.filesDir

        //delete the LISTS file
        File(directory, filename).delete()
    }
}