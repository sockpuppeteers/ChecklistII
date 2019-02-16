package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import android.content.Intent
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


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

        var user = UserPage(0, "", "", "", "")

        login_button.setOnClickListener {
            val db = Database(eUserName.text.toString())
            user = db.LogIn(ePW.text.toString())
            if (!user.ErrorCheck())
            {
                if (user.ViewError() == "failed")
                {
                    ErrorText.text = "Failed to connect to server"
                }
                else if (user.ViewError() == "{\"Message\":\"username or password is wrong dude\"}")
                {
                    ErrorText.text = "Wrong username or password"
                }
                else if (user.ViewError() == "{\"Message\":\"An error has occurred.\"}")
                {
                    ErrorText.text = "Something weird went wrong"
                }
            }
            else
            {
                val tempIntent = Intent(this, UserLogin::class.java).apply {
                        putExtra("uname", user.ViewUserName())
                        putExtra("fname", user.ViewFName())
                        putExtra("lname", user.ViewLName())
                }
                startActivity(tempIntent)
            }
        }
    }
}

//class AllUsers(val users: List<UserPage>)
//{
//
//}
//
//class UserTemp(val UserID: Int, val Username: String, val Fname: String, val Lname: String, private val pw: String)
//{
//
//}