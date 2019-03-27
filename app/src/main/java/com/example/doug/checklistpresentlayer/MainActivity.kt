package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_login.*


class MainActivity : AppCompatActivity() {

    var body: String? = ""
//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        when (item.itemId) {
//            R.id.navigation_home -> {
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_dashboard -> {
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_notifications -> {
//                return@OnNavigationItemSelectedListener true
//            }
//        }
//        false
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var user: UserPage

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
}