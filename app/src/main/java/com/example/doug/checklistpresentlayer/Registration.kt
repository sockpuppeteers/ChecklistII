package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.content.pm.ActivityInfo
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*
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
                var error: String = db.RegisterUser(rUserName.text.toString(),
                    rEmail.text.toString(), rFName.text.toString(), rLName.text.toString(),
                    rPW1.text.toString())
                if (error != "")
                    rErrorText.text = error
                //else will be called if there were no errors
                else {
                    //send the data to the next page
                    val tempIntent = Intent(this, BaseListofLists::class.java).apply {
                        putExtra("uname", rUserName.text.toString())
                        putExtra("fname", rFName.text.toString())
                        putExtra("lname", rLName.text.toString())
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
}