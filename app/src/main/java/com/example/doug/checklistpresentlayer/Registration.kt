package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*


class Registration : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        register_button.setOnClickListener {
            val db = Database(rUserName.text.toString())
            var error: String = db.RegisterUser(rEmail.text.toString(),rFName.text.toString(),rLName.text.toString(),
                rPW1.text.toString(),rPW2.text.toString())
            if (error != "")
                rErrorText.text = error
            else
            {
                val tempIntent = Intent(this, UserLogin::class.java).apply {
                        putExtra("uname", rUserName.text.toString())
                        putExtra("fname", rFName.text.toString())
                        putExtra("lname", rLName.text.toString())
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