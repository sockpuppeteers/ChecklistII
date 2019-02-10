package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.example.doug.checklistpresentlayer.R.drawable.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import android.content.Intent
import android.view.View

class UserLogin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val UName = intent.getStringExtra("uname")
        val FName = intent.getStringExtra("fname")
        val LName = intent.getStringExtra("lname")
        FLName.text = FName + " " + LName
        UserName.text = "(" + UName + ")"

        Icon.setImageResource(icon1)

        ToCheckList.setOnClickListener {
            val tempIntent = Intent(this, BaseListofLists::class.java)
            startActivity(tempIntent)
        }
    }
}